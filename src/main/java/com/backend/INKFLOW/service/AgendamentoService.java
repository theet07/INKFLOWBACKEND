package com.backend.INKFLOW.service;

import com.backend.INKFLOW.model.Agendamento;
import com.backend.INKFLOW.model.Cliente;
import com.backend.INKFLOW.repository.AgendamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AgendamentoService {

    private static final String CLOUDINARY_PREFIX = "https://res.cloudinary.com";

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    /** Retorna todos os agendamentos ordenados por data, para uso exclusivo do ADMIN. */
    public List<Agendamento> getAllAgendamentos() {
        return agendamentoRepository.findAllByOrderByDataHoraAsc();
    }

    public Optional<Agendamento> getAgendamentoById(Long id) {
        return agendamentoRepository.findById(id);
    }

    public List<Agendamento> getAgendamentosByCliente(Cliente cliente) {
        return agendamentoRepository.findByCliente(cliente);
    }

    public List<Agendamento> getAgendamentosByClienteId(Long clienteId) {
        return agendamentoRepository.findByClienteId(clienteId);
    }

    /**
     * Busca agendamentos pelo ID do artista. Usado apenas pelo ADMIN,
     * que pode consultar qualquer artista sem restricao de ownership.
     */
    public List<Agendamento> getAgendamentosByArtistaId(Integer artistaId) {
        return agendamentoRepository.findByArtistaIdOrderByDataHoraAsc(artistaId);
    }

    /**
     * Busca agendamentos pelo e-mail do artista extraido diretamente do Token JWT.
     * Este metodo e a fonte da verdade para o dashboard do artista — o ID da URL
     * nunca e usado como parametro de consulta, prevenindo ataques IDOR.
     */
    public List<Agendamento> getAgendamentosByArtistaEmail(String email) {
        return agendamentoRepository.findByArtistaEmailOrderByDataHoraAsc(email);
    }

    public List<Agendamento> getAgendamentosByStatus(String status) {
        return agendamentoRepository.findByStatus(status);
    }

    public List<Agendamento> getAgendamentosByPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return agendamentoRepository.findByDataHoraBetween(inicio, fim);
    }

    /**
     * Salva um agendamento. Valida que a imagemReferenciaUrl, se presente,
     * pertence ao Cloudinary para evitar links externos maliciosos.
     */
    public Agendamento saveAgendamento(Agendamento agendamento) {
        String url = agendamento.getImagemReferenciaUrl();
        if (url != null && !url.isBlank() && !url.startsWith(CLOUDINARY_PREFIX)) {
            throw new IllegalArgumentException("imagemReferenciaUrl deve ser uma URL valida do Cloudinary.");
        }
        return agendamentoRepository.save(agendamento);
    }

    public Optional<Agendamento> updateStatus(Long id, String status, Integer avaliacao, String observacoes) {
        return agendamentoRepository.findById(id).map(ag -> {
            ag.setStatus(status);
            if (avaliacao != null) ag.setAvaliacao(avaliacao);
            if (observacoes != null) ag.setObservacoes(observacoes);
            return agendamentoRepository.save(ag);
        });
    }

    public void deleteAgendamento(Long id) {
        agendamentoRepository.deleteById(id);
    }
}
