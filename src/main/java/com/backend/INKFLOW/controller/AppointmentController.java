package com.backend.INKFLOW.controller;

import com.backend.INKFLOW.model.Agendamento;
import com.backend.INKFLOW.service.AgendamentoService;
import com.backend.INKFLOW.service.ArtistaService;
import com.backend.INKFLOW.service.ClienteService;
import com.backend.INKFLOW.service.DisponibilidadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Alias em ingles para o fluxo de agendamento do Booking.jsx.
 * Recebe POST /api/appointments e delega para o AgendamentoService.
 * Publico — nao requer token JWT (cliente pode agendar sem conta).
 */
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AgendamentoService agendamentoService;

    @Autowired
    private ArtistaService artistaService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DisponibilidadeService disponibilidadeService;

    @Value("${landing.default.client.password:inkflow@landing2025}")
    private String defaultClientPassword;

    /**
     * POST /api/appointments
     * Cria um agendamento vindo do formulario Booking.jsx.
     * Aceita o mesmo payload do LandingPageController /api/v1/appointments.
     * Status forcado para PENDENTE no servidor.
     */
    @PostMapping
    public ResponseEntity<?> createAppointment(@RequestBody Map<String, Object> body) {
        Object artistIdRaw = body.get("artista");
        Object clienteRaw = body.get("cliente");
        String dataHoraStr = (String) body.get("dataHora");
        String descricao = (String) body.get("descricao");

        // Suporte ao payload direto do Booking.jsx
        // { cliente: {id}, artista: {id}, dataHora, servico, descricao, ... }
        if (artistIdRaw instanceof Map && clienteRaw instanceof Map && dataHoraStr != null) {
            return criarAgendamentoDireto(body);
        }

        // Suporte ao payload da Landing Page
        // { artistId, clienteEmail, date, time, description, ... }
        Object artistIdV1 = body.get("artistId");
        String clienteEmail = (String) body.get("clienteEmail");
        if (artistIdV1 != null && clienteEmail != null) {
            return criarAgendamentoLandingPage(body);
        }

        return ResponseEntity.badRequest()
                .body(Map.of("message", "Payload invalido. Envie {cliente:{id}, artista:{id}, dataHora} ou {artistId, clienteEmail, date}."));
    }

    /** Payload direto: { cliente:{id}, artista:{id}, dataHora, servico, ... } */
    private ResponseEntity<?> criarAgendamentoDireto(Map<String, Object> body) {
        try {
            Agendamento ag = new Agendamento();

            // Cliente
            Map<?, ?> clienteMap = (Map<?, ?>) body.get("cliente");
            Long clienteId = ((Number) clienteMap.get("id")).longValue();
            clienteService.getClienteById(clienteId).ifPresent(ag::setCliente);
            if (ag.getCliente() == null)
                return ResponseEntity.badRequest().body(Map.of("message", "Cliente nao encontrado."));

            // Artista (opcional)
            Object artistaRaw = body.get("artista");
            if (artistaRaw instanceof Map) {
                Object artistaIdRaw = ((Map<?, ?>) artistaRaw).get("id");
                if (artistaIdRaw != null) {
                    Integer artistaId = ((Number) artistaIdRaw).intValue();
                    artistaService.getById(artistaId).ifPresent(ag::setArtista);
                }
            }

            ag.setDataHora(LocalDateTime.parse((String) body.get("dataHora")));
            ag.setStatus("PENDENTE");
            if (body.get("servico") != null) ag.setServico((String) body.get("servico"));
            if (body.get("descricao") != null) ag.setDescricao((String) body.get("descricao"));
            if (body.get("regiao") != null) ag.setRegiao((String) body.get("regiao"));
            if (body.get("largura") instanceof Number) ag.setLargura(((Number) body.get("largura")).doubleValue());
            if (body.get("altura") instanceof Number) ag.setAltura(((Number) body.get("altura")).doubleValue());
            if (body.get("tags") != null) ag.setTags((String) body.get("tags"));
            if (body.get("imagemReferenciaUrl") != null) ag.setImagemReferenciaUrl((String) body.get("imagemReferenciaUrl"));

            Agendamento salvo = agendamentoService.saveAgendamento(ag);
            return ResponseEntity.status(201).body(salvo);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Erro ao criar agendamento: " + e.getMessage()));
        }
    }

    /** Payload landing page: { artistId, clienteEmail, date, time, description, ... } */
    private ResponseEntity<?> criarAgendamentoLandingPage(Map<String, Object> body) {
        Integer artistId = ((Number) body.get("artistId")).intValue();
        String clienteEmail = (String) body.get("clienteEmail");
        String clienteNome = (String) body.get("clienteNome");
        String date = (String) body.get("date");
        String time = (String) body.get("time");
        String description = (String) body.get("description");

        com.backend.INKFLOW.model.Cliente cliente = clienteService.getUserByEmail(clienteEmail)
                .orElseGet(() -> {
                    com.backend.INKFLOW.model.Cliente novo = new com.backend.INKFLOW.model.Cliente();
                    novo.setEmail(clienteEmail);
                    novo.setUsername(clienteEmail.split("@")[0] + "_" + System.currentTimeMillis() % 10000);
                    novo.setFullName(clienteNome != null ? clienteNome : clienteEmail.split("@")[0]);
                    novo.setTelefone((String) body.get("clienteTelefone"));
                    novo.setPassword(passwordEncoder.encode(defaultClientPassword));
                    return clienteService.saveCliente(novo);
                });

        Optional<com.backend.INKFLOW.model.Artista> artistaOpt = artistaService.getById(artistId);
        if (artistaOpt.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("message", "Artista nao encontrado."));

        LocalDateTime dataHora;
        try {
            String timeStr = time != null ? time : "12:00";
            dataHora = LocalDateTime.parse(date + "T" + timeStr + ":00");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Formato de data/hora invalido."));
        }

        Agendamento ag = new Agendamento();
        ag.setCliente(cliente);
        ag.setArtista(artistaOpt.get());
        ag.setDataHora(dataHora);
        ag.setDescricao(description);
        ag.setStatus("PENDENTE");
        ag.setServico(body.get("estilo") != null
                ? body.get("estilo") + " com " + artistaOpt.get().getNome()
                : "Sessao com " + artistaOpt.get().getNome());
        if (body.get("regiao") != null) ag.setRegiao((String) body.get("regiao"));
        if (body.get("largura") instanceof Number) ag.setLargura(((Number) body.get("largura")).doubleValue());
        if (body.get("altura") instanceof Number) ag.setAltura(((Number) body.get("altura")).doubleValue());
        if (body.get("tags") != null) ag.setTags((String) body.get("tags"));
        if (body.get("imagemReferenciaUrl") != null) ag.setImagemReferenciaUrl((String) body.get("imagemReferenciaUrl"));

        Agendamento salvo = agendamentoService.saveAgendamento(ag);

        Map<String, Object> resposta = new HashMap<>();
        resposta.put("success", true);
        resposta.put("id", salvo.getId());
        resposta.put("status", salvo.getStatus());
        resposta.put("message", "Solicitacao enviada com sucesso!");
        return ResponseEntity.status(201).body(resposta);
    }
}
