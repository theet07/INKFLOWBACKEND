package com.backend.INKFLOW.service;

import com.backend.INKFLOW.model.Agendamento;
import com.backend.INKFLOW.model.Cliente;
import com.backend.INKFLOW.repository.AgendamentoRepository;
import com.backend.INKFLOW.repository.ClienteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AgendamentoService {

    private static final Logger log = LoggerFactory.getLogger(AgendamentoService.class);
    private static final String CLOUDINARY_PREFIX = "https://res.cloudinary.com";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm[:ss]");

    @Autowired private AgendamentoRepository agendamentoRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private ClienteService clienteService;
    @Autowired private ArtistaService artistaService;
    @Autowired private PasswordEncoder passwordEncoder;

    @Value("${landing.default.client.password}")
    private String defaultClientPassword;

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

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

    public List<Agendamento> getAgendamentosByArtistaId(Integer artistaId) {
        return agendamentoRepository.findByArtistaIdOrderByDataHoraAsc(artistaId);
    }

    public List<Agendamento> getAgendamentosByArtistaEmail(String email) {
        return agendamentoRepository.findByArtistaEmailOrderByDataHoraAsc(email);
    }

    public List<Agendamento> getAgendamentosByStatus(String status) {
        return agendamentoRepository.findByStatus(status);
    }

    public List<Agendamento> getAgendamentosByPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return agendamentoRepository.findByDataHoraBetween(inicio, fim);
    }

    // -------------------------------------------------------------------------
    // Criacao — Unica Fonte da Verdade
    // -------------------------------------------------------------------------

    /** Payload do Booking.jsx: { cliente:{id}, artista:{id}, dataHora, ... } */
    public Agendamento criarAgendamentoDireto(Map<String, Object> body) {
        Map<?, ?> clienteMap = (Map<?, ?>) body.get("cliente");
        if (clienteMap == null || clienteMap.get("id") == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cliente.id e obrigatorio.");
        Long clienteId = ((Number) clienteMap.get("id")).longValue();
        var cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente id=" + clienteId + " nao encontrado."));

        Map<?, ?> artistaMap = (Map<?, ?>) body.get("artista");
        if (artistaMap == null || artistaMap.get("id") == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "artista.id e obrigatorio.");
        Integer artistaId = ((Number) artistaMap.get("id")).intValue();
        var artista = artistaService.getById(artistaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artista id=" + artistaId + " nao encontrado."));

        String dataHoraStr = (String) body.get("dataHora");
        if (dataHoraStr == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dataHora e obrigatorio.");
        LocalDateTime dataHora;
        try {
            dataHora = LocalDateTime.parse(dataHoraStr, DATE_FMT);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de dataHora invalido. Use yyyy-MM-ddTHH:mm:ss");
        }

        // Validação: não permite agendamento no passado
        if (dataHora.isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível agendar para uma data no passado.");
        }

        Agendamento ag = new Agendamento();
        ag.setCliente(cliente);
        ag.setArtista(artista);
        ag.setDataHora(dataHora);
        ag.setStatus("PENDENTE");
        if (body.get("servico") != null)   ag.setServico((String) body.get("servico"));
        if (body.get("descricao") != null)  ag.setDescricao((String) body.get("descricao"));
        if (body.get("regiao") != null)     ag.setRegiao((String) body.get("regiao"));
        if (body.get("largura") instanceof Number) ag.setLargura(((Number) body.get("largura")).doubleValue());
        if (body.get("altura")  instanceof Number) ag.setAltura(((Number) body.get("altura")).doubleValue());
        if (body.get("tags") != null)       ag.setTags((String) body.get("tags"));
        if (body.get("imagemReferenciaUrl") != null) ag.setImagemReferenciaUrl((String) body.get("imagemReferenciaUrl"));

        // Validação anti double-booking: verifica conflito de horário
        verificarConflito(artistaId, dataHora);

        log.info("[Agendamento] Direto: clienteId={} artistaId={} dataHora={}", clienteId, artistaId, dataHora);
        return agendamentoRepository.save(ag);
    }

    /** Payload da Landing Page: { artistId, clienteEmail, date, time, ... } */
    public Agendamento criarAgendamentoLandingPage(Map<String, Object> body) {
        Object artistIdRaw = body.get("artistId");
        String clienteEmail = (String) body.get("clienteEmail");
        String date = (String) body.get("date");
        if (artistIdRaw == null || clienteEmail == null || date == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campos obrigatorios: artistId, clienteEmail, date.");

        Integer artistId = ((Number) artistIdRaw).intValue();
        var artista = artistaService.getById(artistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artista nao encontrado."));

        String clienteNome = (String) body.get("clienteNome");
        Cliente cliente = clienteService.getUserByEmail(clienteEmail).orElseGet(() -> {
            Cliente novo = new Cliente();
            novo.setEmail(clienteEmail);
            novo.setUsername(clienteEmail.split("@")[0] + "_" + System.currentTimeMillis() % 10000);
            novo.setFullName(clienteNome != null ? clienteNome : clienteEmail.split("@")[0]);
            novo.setTelefone((String) body.get("clienteTelefone"));
            novo.setPassword(passwordEncoder.encode(defaultClientPassword));
            return clienteService.saveCliente(novo);
        });

        String time = (String) body.get("time");
        LocalDateTime dataHora;
        try {
            dataHora = LocalDateTime.parse(date + "T" + (time != null ? time : "12:00") + ":00");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de data/hora invalido. Use date: YYYY-MM-DD e time: HH:mm.");
        }

        // Validação: não permite agendamento no passado
        if (dataHora.isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível agendar para uma data no passado.");
        }

        Agendamento ag = new Agendamento();
        ag.setCliente(cliente);
        ag.setArtista(artista);
        ag.setDataHora(dataHora);
        ag.setDescricao((String) body.get("description"));
        ag.setStatus("PENDENTE");
        ag.setServico(body.get("estilo") != null
                ? body.get("estilo") + " com " + artista.getNome()
                : "Sessao com " + artista.getNome());
        if (body.get("regiao") != null)     ag.setRegiao((String) body.get("regiao"));
        if (body.get("largura") instanceof Number) ag.setLargura(((Number) body.get("largura")).doubleValue());
        if (body.get("altura")  instanceof Number) ag.setAltura(((Number) body.get("altura")).doubleValue());
        if (body.get("tags") != null)       ag.setTags((String) body.get("tags"));
        if (body.get("imagemReferenciaUrl") != null) ag.setImagemReferenciaUrl((String) body.get("imagemReferenciaUrl"));

        // Validação anti double-booking: verifica conflito de horário
        verificarConflito(artistId, dataHora);

        log.info("[Agendamento] LandingPage: clienteEmail={} artistaId={} dataHora={}", clienteEmail, artistId, dataHora);
        return agendamentoRepository.save(ag);
    }

    // -------------------------------------------------------------------------
    // Atualizacoes
    // -------------------------------------------------------------------------

    public Agendamento saveAgendamento(Agendamento agendamento) {
        if (agendamento.getCliente() == null || agendamento.getCliente().getId() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cliente e obrigatorio.");
        agendamento.setCliente(
            clienteRepository.findById(agendamento.getCliente().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Cliente id=" + agendamento.getCliente().getId() + " nao encontrado."))
        );
        if (agendamento.getArtista() != null && agendamento.getArtista().getId() != null) {
            agendamento.setArtista(
                artistaService.getById(agendamento.getArtista().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Artista id=" + agendamento.getArtista().getId() + " nao encontrado."))
            );
        }
        String url = agendamento.getImagemReferenciaUrl();
        if (url != null && !url.isBlank() && url.startsWith("http") && !url.startsWith(CLOUDINARY_PREFIX))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "imagemReferenciaUrl deve ser uma URL valida do Cloudinary.");
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

    // -------------------------------------------------------------------------
    // Validação de conflitos
    // -------------------------------------------------------------------------

    /**
     * Verifica se já existe um agendamento ativo no mesmo horário para o artista.
     * Previne double-booking quando dois clientes tentam agendar simultaneamente.
     */
    private void verificarConflito(Integer artistaId, LocalDateTime dataHora) {
        List<Agendamento> ocupados = agendamentoRepository.findOcupadosByArtistaIdAndDia(
                artistaId,
                dataHora.toLocalDate().atStartOfDay(),
                dataHora.toLocalDate().plusDays(1).atStartOfDay());
        boolean horarioOcupado = ocupados.stream()
                .anyMatch(c -> c.getDataHora().equals(dataHora));
        if (horarioOcupado) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Este horário já está ocupado. Escolha outro horário.");
        }
    }
}
