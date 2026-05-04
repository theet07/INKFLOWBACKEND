package com.backend.INKFLOW.service;

import com.backend.INKFLOW.model.*;
import com.backend.INKFLOW.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class CicatrizacaoService {

    private static final Logger log = LoggerFactory.getLogger(CicatrizacaoService.class);

    @Autowired private CicatrizacaoRepository cicatrizacaoRepository;
    @Autowired private CheckpointDiaRepository checkpointDiaRepository;
    @Autowired private ChecklistItemRepository checklistItemRepository;
    @Autowired private AgendamentoRepository agendamentoRepository;

    // -------------------------------------------------------------------------
    // Iniciar cicatrização
    // -------------------------------------------------------------------------

    @Transactional
    public Cicatrizacao iniciar(Long agendamentoId) {
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agendamento nao encontrado."));

        if (cicatrizacaoRepository.findByAgendamentoId(agendamentoId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cicatrizacao ja existe para este agendamento.");
        }

        int periodo = calcularPeriodo(agendamento.getRegiao(), agendamento.getLargura(), agendamento.getAltura());
        LocalDate hoje = LocalDate.now();

        Cicatrizacao cic = new Cicatrizacao();
        cic.setAgendamento(agendamento);
        cic.setDataInicio(hoje);
        cic.setDataFim(hoje.plusDays(periodo - 1));
        cic.setPeriodoTotalDias(periodo);
        cic.setStatus("ATIVA");
        cic.setDiaAtual(1);
        cic.setFaseAtual("FASE_1_PRIMEIRAS_24H");
        cicatrizacaoRepository.save(cic);

        gerarCheckpoints(cic, periodo, hoje);

        log.info("[Cicatrizacao] Iniciada para agendamentoId={} periodo={}dias", agendamentoId, periodo);
        return cic;
    }

    // -------------------------------------------------------------------------
    // Buscar cicatrização ativa
    // -------------------------------------------------------------------------

    public Optional<Cicatrizacao> buscarAtiva(Long clienteId) {
        return cicatrizacaoRepository.findAtivaByClienteId(clienteId);
    }

    // -------------------------------------------------------------------------
    // Buscar caminho completo
    // -------------------------------------------------------------------------

    public List<CheckpointDia> buscarCaminho(Long cicatrizacaoId) {
        atualizarStatusDias(cicatrizacaoId);
        return checkpointDiaRepository.findByCicatrizacaoIdOrderByNumeroDiaAsc(cicatrizacaoId);
    }

    // -------------------------------------------------------------------------
    // Checklist do dia
    // -------------------------------------------------------------------------

    public List<ChecklistItem> buscarChecklistDia(Long cicatrizacaoId, Integer numeroDia) {
        CheckpointDia checkpoint = checkpointDiaRepository
                .findByCicatrizacaoIdAndNumeroDia(cicatrizacaoId, numeroDia)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dia nao encontrado."));
        return checklistItemRepository.findByCheckpointDiaIdOrderByPeriodoAscOrdemAsc(checkpoint.getId());
    }

    // -------------------------------------------------------------------------
    // Toggle item do checklist
    // -------------------------------------------------------------------------

    @Transactional
    public CheckpointDia toggleItem(Long cicatrizacaoId, Long itemId) {
        ChecklistItem item = checklistItemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item nao encontrado."));

        CheckpointDia checkpoint = item.getCheckpointDia();
        if (!checkpoint.getCicatrizacao().getId().equals(cicatrizacaoId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Item nao pertence a esta cicatrizacao.");
        }

        boolean novoConcluido = !item.getConcluido();
        item.setConcluido(novoConcluido);
        item.setDataMarcacao(novoConcluido ? java.time.LocalDateTime.now() : null);
        checklistItemRepository.save(item);

        recalcularCheckpoint(checkpoint);
        return checkpoint;
    }

    // -------------------------------------------------------------------------
    // Lógica interna — cálculo de período
    // -------------------------------------------------------------------------

    public int calcularPeriodo(String regiao, Double largura, Double altura) {
        int fatorBase = 30;
        if (regiao != null) {
            fatorBase = switch (regiao.toLowerCase().trim()) {
                case "braço", "braco" -> 25;
                case "perna" -> 28;
                case "costas" -> 30;
                case "peito" -> 28;
                case "pescoço", "pescoco" -> 21;
                case "mão", "mao" -> 35;
                case "pé", "pe" -> 35;
                default -> 30;
            };
        }

        double multiplicador = 1.0;
        if (largura != null && altura != null && largura > 0 && altura > 0) {
            double area = largura * altura;
            multiplicador = area <= 50 ? 0.85
                          : area <= 200 ? 1.0
                          : area <= 500 ? 1.15
                          : 1.3;
        }

        int resultado = (int) Math.round(fatorBase * multiplicador);
        return Math.max(21, Math.min(45, resultado));
    }

    // -------------------------------------------------------------------------
    // Lógica interna — geração de checkpoints e checklist
    // -------------------------------------------------------------------------

    private void gerarCheckpoints(Cicatrizacao cic, int periodo, LocalDate inicio) {
        for (int dia = 1; dia <= periodo; dia++) {
            CheckpointDia cp = new CheckpointDia();
            cp.setCicatrizacao(cic);
            cp.setNumeroDia(dia);
            cp.setFase(resolverFase(dia));
            cp.setData(inicio.plusDays(dia - 1));
            cp.setTemQuiz(dia == 1 || dia == 7 || dia == 14 || dia == periodo);
            cp.setStatusDia(dia == 1 ? "DISPONIVEL" : "BLOQUEADO");
            checkpointDiaRepository.save(cp);

            gerarChecklistItens(cp, dia);
        }
    }

    private void gerarChecklistItens(CheckpointDia cp, int dia) {
        String fase = cp.getFase();
        List<String[]> itensManha = getItensManha(fase);
        List<String[]> itensTarde = getItensTarde(fase);
        List<String[]> itensNoite = getItensNoite(fase);

        int ordem = 1;
        for (String[] item : itensManha) {
            salvarItem(cp, "MANHA", ordem++, item[0]);
        }
        for (String[] item : itensTarde) {
            salvarItem(cp, "TARDE", ordem++, item[0]);
        }
        for (String[] item : itensNoite) {
            salvarItem(cp, "NOITE", ordem++, item[0]);
        }
    }

    private void salvarItem(CheckpointDia cp, String periodo, int ordem, String descricao) {
        ChecklistItem item = new ChecklistItem();
        item.setCheckpointDia(cp);
        item.setPeriodo(periodo);
        item.setOrdem(ordem);
        item.setDescricao(descricao);
        checklistItemRepository.save(item);
    }

    private String resolverFase(int dia) {
        if (dia == 1) return "FASE_1_PRIMEIRAS_24H";
        if (dia <= 7) return "FASE_2_INICIAL";
        if (dia <= 14) return "FASE_3_DESCAMACAO";
        return "FASE_4_PROFUNDA";
    }

    // -------------------------------------------------------------------------
    // Lógica interna — recalcular estrelas e XP do checkpoint
    // -------------------------------------------------------------------------

    private void recalcularCheckpoint(CheckpointDia checkpoint) {
        List<ChecklistItem> itens = checklistItemRepository
                .findByCheckpointDiaIdOrderByPeriodoAscOrdemAsc(checkpoint.getId());

        long total = itens.size();
        long concluidos = itens.stream().filter(ChecklistItem::getConcluido).count();

        if (total == 0) return;

        double pct = (double) concluidos / total;
        int estrelas = pct == 1.0 ? 3 : pct >= 0.5 ? 2 : pct > 0 ? 1 : 0;
        int xp = (int) (concluidos * 5) + (pct == 1.0 ? 40 : 0);

        checkpoint.setEstrelas(estrelas);
        checkpoint.setXpGanho(xp);
        checkpoint.setStatusDia(pct == 1.0 ? "COMPLETO" : pct > 0 ? "PARCIAL" : "DISPONIVEL");
        checkpointDiaRepository.save(checkpoint);

        // Atualiza XP total da cicatrização
        Cicatrizacao cic = checkpoint.getCicatrizacao();
        List<CheckpointDia> todos = checkpointDiaRepository
                .findByCicatrizacaoIdOrderByNumeroDiaAsc(cic.getId());
        int xpTotal = todos.stream().mapToInt(CheckpointDia::getXpGanho).sum();
        cic.setXpTotal(xpTotal);
        cicatrizacaoRepository.save(cic);
    }

    // -------------------------------------------------------------------------
    // Lógica interna — atualizar status dos dias (DISPONIVEL/PERDIDO)
    // -------------------------------------------------------------------------

    private void atualizarStatusDias(Long cicatrizacaoId) {
        LocalDate hoje = LocalDate.now();
        List<CheckpointDia> checkpoints = checkpointDiaRepository
                .findByCicatrizacaoIdOrderByNumeroDiaAsc(cicatrizacaoId);

        for (CheckpointDia cp : checkpoints) {
            if ("COMPLETO".equals(cp.getStatusDia()) || "PARCIAL".equals(cp.getStatusDia())) continue;

            if (cp.getData().isEqual(hoje)) {
                cp.setStatusDia("DISPONIVEL");
                checkpointDiaRepository.save(cp);
            } else if (cp.getData().isBefore(hoje) && "BLOQUEADO".equals(cp.getStatusDia())) {
                cp.setStatusDia("PERDIDO");
                checkpointDiaRepository.save(cp);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Conteúdo do checklist por fase
    // -------------------------------------------------------------------------

    private List<String[]> getItensManha(String fase) {
        return switch (fase) {
            case "FASE_1_PRIMEIRAS_24H" -> List.of(
                new String[]{"Manter o curativo original por pelo menos 2-4 horas"},
                new String[]{"Não molhar a tatuagem"},
                new String[]{"Evitar exposição ao sol"}
            );
            case "FASE_2_INICIAL" -> List.of(
                new String[]{"Lavar suavemente com água morna e sabão neutro"},
                new String[]{"Secar com papel toalha limpo (sem esfregar)"},
                new String[]{"Aplicar camada fina de pomada cicatrizante"}
            );
            case "FASE_3_DESCAMACAO" -> List.of(
                new String[]{"Lavar suavemente com água morna e sabão neutro"},
                new String[]{"Secar com papel toalha limpo"},
                new String[]{"Aplicar hidratante sem perfume"}
            );
            default -> List.of(
                new String[]{"Lavar suavemente com água morna"},
                new String[]{"Aplicar hidratante sem perfume"},
                new String[]{"Verificar sinais de infecção"}
            );
        };
    }

    private List<String[]> getItensTarde(String fase) {
        return switch (fase) {
            case "FASE_1_PRIMEIRAS_24H" -> List.of(
                new String[]{"Verificar se o curativo está íntegro"},
                new String[]{"Evitar roupas apertadas sobre a tatuagem"},
                new String[]{"Não coçar ou tocar desnecessariamente"}
            );
            case "FASE_2_INICIAL" -> List.of(
                new String[]{"Reaplicar pomada cicatrizante se necessário"},
                new String[]{"Evitar exposição ao sol diretamente"},
                new String[]{"Não mergulhar em piscina ou mar"}
            );
            case "FASE_3_DESCAMACAO" -> List.of(
                new String[]{"Não arrancar as cascas — deixar cair naturalmente"},
                new String[]{"Reaplicar hidratante se sentir ressecamento"},
                new String[]{"Evitar exposição ao sol sem protetor"}
            );
            default -> List.of(
                new String[]{"Reaplicar hidratante se necessário"},
                new String[]{"Evitar exposição prolongada ao sol"},
                new String[]{"Usar protetor solar FPS 50+ se exposto ao sol"}
            );
        };
    }

    private List<String[]> getItensNoite(String fase) {
        return switch (fase) {
            case "FASE_1_PRIMEIRAS_24H" -> List.of(
                new String[]{"Remover o curativo com cuidado após 4-6 horas"},
                new String[]{"Lavar suavemente com água morna e sabão neutro"},
                new String[]{"Aplicar camada fina de pomada cicatrizante e deixar sem curativo"}
            );
            case "FASE_2_INICIAL" -> List.of(
                new String[]{"Lavar suavemente antes de dormir"},
                new String[]{"Aplicar pomada cicatrizante"},
                new String[]{"Dormir com lençol limpo — evitar tecidos sintéticos"}
            );
            case "FASE_3_DESCAMACAO" -> List.of(
                new String[]{"Lavar suavemente antes de dormir"},
                new String[]{"Aplicar hidratante generosamente"},
                new String[]{"Dormir com lençol limpo"}
            );
            default -> List.of(
                new String[]{"Lavar suavemente antes de dormir"},
                new String[]{"Aplicar hidratante"},
                new String[]{"Verificar aparência geral da tatuagem"}
            );
        };
    }
}
