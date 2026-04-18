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
        if (artistaId == null || data == null) return List.of();

        int diaSemana = data.getDayOfWeek().getValue() - 1;

        Optional<DisponibilidadeArtista> dispOpt =
                disponibilidadeRepository.findByArtistaIdAndDiaSemana(artistaId, diaSemana);

        if (dispOpt.isEmpty() || Boolean.FALSE.equals(dispOpt.get().getAtivo())) {
            return List.of();
        }

        DisponibilidadeArtista disp = dispOpt.get();

        if (disp.getHoraInicio() == null || disp.getHoraFim() == null) return List.of();

        LocalTime inicio = LocalTime.parse(disp.getHoraInicio(), TIME_FMT);
        LocalTime fim = LocalTime.parse(disp.getHoraFim(), TIME_FMT);
        int slot = disp.getDuracaoSlotMinutos() != null ? disp.getDuracaoSlotMinutos() : 60;

        LocalDateTime inicioDia = data.atStartOfDay();
        LocalDateTime fimDia = data.plusDays(1).atStartOfDay();

        List<Agendamento> ocupadosRaw = agendamentoRepository
                .findOcupadosByArtistaIdAndDia(artistaId, inicioDia, fimDia);

        // Protege contra lista nula retornada pelo banco
        List<Agendamento> ocupados = ocupadosRaw != null ? ocupadosRaw : List.of();

        Set<String> horariosOcupados = ocupados.stream()
                .filter(a -> a.getDataHora() != null)
                .map(a -> a.getDataHora().toLocalTime().format(TIME_FMT))
                .collect(Collectors.toSet());

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
     * Usa HashMap para evitar NullPointerException do Collectors.toMap
     * quando a lista de slots e vazia.
     */
    public Map<String, List<String>> getCalendarioMensal(Integer artistaId, int ano, int mes) {
        LocalDate inicio = LocalDate.of(ano, mes, 1);
        LocalDate fim = inicio.withDayOfMonth(inicio.lengthOfMonth());
        LocalDate hoje = LocalDate.now();

        Map<String, List<String>> resultado = new java.util.LinkedHashMap<>();
        inicio.datesUntil(fim.plusDays(1))
                .filter(d -> !d.isBefore(hoje))
                .forEach(d -> {
                    try {
                        List<String> slots = getSlotsDisponiveis(artistaId, d);
                        resultado.put(d.toString(), slots != null ? slots : List.of());
                    } catch (Exception e) {
                        resultado.put(d.toString(), List.of());
                    }
                });
        return resultado;
    }

    public void remover(Long id) {
        disponibilidadeRepository.findById(id).ifPresent(d -> {
            d.setAtivo(false);
            disponibilidadeRepository.save(d);
        });
    }
}
