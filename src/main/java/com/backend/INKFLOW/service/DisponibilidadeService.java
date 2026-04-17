package com.backend.INKFLOW.service;

import com.backend.INKFLOW.model.Agendamento;
import com.backend.INKFLOW.model.DisponibilidadeArtista;
import com.backend.INKFLOW.repository.AgendamentoRepository;
import com.backend.INKFLOW.repository.DisponibilidadeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DisponibilidadeService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @Autowired
    private DisponibilidadeRepository disponibilidadeRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private ArtistaService artistaService;

    /** Retorna toda a grade semanal de disponibilidade de um artista. */
    public List<DisponibilidadeArtista> getByArtista(Integer artistaId) {
        return disponibilidadeRepository.findByArtistaIdAndAtivoTrue(artistaId);
    }

    /**
     * Salva ou atualiza a disponibilidade de um dia da semana para um artista.
     * Usado pelo artista no painel de configuracoes.
     */
    public Optional<DisponibilidadeArtista> salvar(Integer artistaId, Integer diaSemana,
                                                    String horaInicio, String horaFim,
                                                    Integer duracaoSlot) {
        return artistaService.getById(artistaId).map(artista -> {
            DisponibilidadeArtista disp = disponibilidadeRepository
                    .findByArtistaIdAndDiaSemana(artistaId, diaSemana)
                    .orElse(new DisponibilidadeArtista());
            disp.setArtista(artista);
            disp.setDiaSemana(diaSemana);
            disp.setHoraInicio(horaInicio);
            disp.setHoraFim(horaFim);
            disp.setDuracaoSlotMinutos(duracaoSlot != null ? duracaoSlot : 60);
            disp.setAtivo(true);
            return disponibilidadeRepository.save(disp);
        });
    }

    /**
     * Gera os slots de horario disponivel para um artista em uma data especifica.
     * Compara a grade semanal com os agendamentos ja existentes e retorna
     * apenas os horarios livres.
     *
     * @return lista de strings "HH:mm" com os slots disponiveis
     */
    public List<String> getSlotsDisponiveis(Integer artistaId, LocalDate data) {
        // Dia da semana: DayOfWeek.MONDAY = 1, convertemos para 0=Seg
        int diaSemana = data.getDayOfWeek().getValue() - 1;

        Optional<DisponibilidadeArtista> dispOpt =
                disponibilidadeRepository.findByArtistaIdAndDiaSemana(artistaId, diaSemana);

        if (dispOpt.isEmpty() || !dispOpt.get().getAtivo()) {
            return List.of(); // artista nao trabalha neste dia
        }

        DisponibilidadeArtista disp = dispOpt.get();
        LocalTime inicio = LocalTime.parse(disp.getHoraInicio(), TIME_FMT);
        LocalTime fim = LocalTime.parse(disp.getHoraFim(), TIME_FMT);
        int slot = disp.getDuracaoSlotMinutos();

        // Busca horarios ja ocupados no banco
        LocalDateTime inicioDia = data.atStartOfDay();
        LocalDateTime fimDia = data.plusDays(1).atStartOfDay();
        List<Agendamento> ocupados = agendamentoRepository
                .findOcupadosByArtistaIdAndDia(artistaId, inicioDia, fimDia);

        Set<String> horariosOcupados = ocupados.stream()
                .map(a -> a.getDataHora().toLocalTime().format(TIME_FMT))
                .collect(Collectors.toSet());

        // Gera todos os slots e filtra os ocupados
        List<String> disponiveis = new ArrayList<>();
        LocalTime atual = inicio;
        while (atual.plusMinutes(slot).compareTo(fim) <= 0) {
            String horario = atual.format(TIME_FMT);
            if (!horariosOcupados.contains(horario)) {
                disponiveis.add(horario);
            }
            atual = atual.plusMinutes(slot);
        }

        return disponiveis;
    }

    /**
     * Retorna um mapa de data -> slots disponiveis para um mes inteiro.
     * Usado pelo calendario do frontend para marcar dias com disponibilidade.
     */
    public Map<String, List<String>> getCalendarioMensal(Integer artistaId, int ano, int mes) {
        LocalDate inicio = LocalDate.of(ano, mes, 1);
        LocalDate fim = inicio.withDayOfMonth(inicio.lengthOfMonth());
        LocalDate hoje = LocalDate.now();

        return inicio.datesUntil(fim.plusDays(1))
                .filter(d -> !d.isBefore(hoje))
                .collect(Collectors.toMap(
                        d -> d.toString(),
                        d -> getSlotsDisponiveis(artistaId, d)
                ));
    }

    public void remover(Long id) {
        disponibilidadeRepository.findById(id).ifPresent(d -> {
            d.setAtivo(false);
            disponibilidadeRepository.save(d);
        });
    }
}
