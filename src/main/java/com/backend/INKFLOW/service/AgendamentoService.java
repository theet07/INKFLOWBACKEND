package com.backend.INKFLOW.service;

import com.backend.INKFLOW.model.Agendamento;
import com.backend.INKFLOW.model.Cliente;
import com.backend.INKFLOW.repository.AgendamentoRepository;
import com.backend.INKFLOW.repository.ArtistaRepository;
import com.backend.INKFLOW.repository.ClienteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AgendamentoService {

    private static final Logger log = LoggerFactory.getLogger(AgendamentoService.class);
    private static final String CLOUDINARY_PREFIX = "https://res.cloudinary.com";

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ArtistaRepository artistaRepository;

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
     * Salva um agendamento com validacao completa de integridade referencial.
     * Carrega Cliente e Artista do banco via findById antes do save,
     * lancando ResponseStatusException se nao encontrados.
     */
    public Agendamento saveAgendamento(Agendamento agendamento) {
        // Valida e carrega Cliente
        if (agendamento.getCliente() == null || agendamento.getCliente().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cliente e obrigatorio.");
        }
        agendamento.setCliente(
            clienteRepository.findById(agendamento.getCliente().getId())
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Cliente id=" + agendamento.getCliente().getId() + " nao encontrado."))
        );

        // Valida e carrega Artista (opcional, mas se informado deve existir)
        if (agendamento.getArtista() != null && agendamento.getArtista().getId() != null) {
            agendamento.setArtista(
                artistaRepository.findById(agendamento.getArtista().getId())
                    .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Artista id=" + agendamento.getArtista().getId() + " nao encontrado."))
            );
        }

        // Valida URL de imagem
        String url = agendamento.getImagemReferenciaUrl();
        if (url != null && !url.isBlank() && url.startsWith("http") && !url.startsWith(CLOUDINARY_PREFIX)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "imagemReferenciaUrl deve ser uma URL valida do Cloudinary.");
        }

        log.info("[AgendamentoService] Salvando: clienteId={} artistaId={} dataHora={}",
                agendamento.getCliente().getId(),
                agendamento.getArtista() != null ? agendamento.getArtista().getId() : "null",
                agendamento.getDataHora());

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

    /** Salva avaliacao do cliente e marca avaliado = true. */
    public Optional<Agendamento> avaliar(Long id, Integer nota, String observacoes) {
        return agendamentoRepository.findById(id).map(ag -> {
            ag.setAvaliacao(nota);
            if (observacoes != null) ag.setObservacoes(observacoes);
            ag.setAvaliado(true);
            return agendamentoRepository.save(ag);
        });
    }

    public void deleteAgendamento(Long id) {
        agendamentoRepository.deleteById(id);
    }
}
