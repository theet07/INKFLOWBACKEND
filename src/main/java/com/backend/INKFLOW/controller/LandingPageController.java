package com.backend.INKFLOW.controller;

import com.backend.INKFLOW.model.Agendamento;
import com.backend.INKFLOW.model.Artista;
import com.backend.INKFLOW.model.Cliente;
import com.backend.INKFLOW.service.AgendamentoService;
import com.backend.INKFLOW.service.ArtistaService;
import com.backend.INKFLOW.service.ClienteService;
import com.backend.INKFLOW.service.DisponibilidadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller v1 dedicado ao fluxo de captura da Landing Page "Para Tatuadores".
 * Todos os endpoints sao publicos — nao requerem token JWT.
 * Nao altera nenhuma rota do dashboard existente.
 */
@RestController
@RequestMapping("/api/v1")
public class LandingPageController {

    @Autowired
    private DisponibilidadeService disponibilidadeService;

    @Autowired
    private AgendamentoService agendamentoService;

    @Autowired
    private ArtistaService artistaService;

    @Autowired
    private ClienteService clienteService;

    /**
     * GET /api/v1/artists/{artistId}/availability?ano=2025&mes=5
     * Retorna o calendario mensal com slots disponiveis por dia.
     * Alimenta o calendario dinamico que o cliente ve na Landing Page.
     */
    @GetMapping("/artists/{artistId}/availability")
    public ResponseEntity<?> getAvailability(@PathVariable Integer artistId,
                                              @RequestParam(required = false) Integer ano,
                                              @RequestParam(required = false) Integer mes) {
        Optional<Artista> artista = artistaService.getById(artistId);
        if (artista.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        int anoParam = ano != null ? ano : LocalDate.now().getYear();
        int mesParam = mes != null ? mes : LocalDate.now().getMonthValue();

        if (mesParam < 1 || mesParam > 12) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mes invalido."));
        }

        Map<String, List<String>> calendario =
                disponibilidadeService.getCalendarioMensal(artistId, anoParam, mesParam);

        return ResponseEntity.ok(Map.of(
                "artistId", artistId,
                "nome", artista.get().getNome(),
                "ano", anoParam,
                "mes", mesParam,
                "disponibilidade", calendario
        ));
    }

    /**
     * GET /api/v1/artists/{artistId}/availability/slots?data=2025-05-10
     * Retorna os slots de horario disponivel para uma data especifica.
     */
    @GetMapping("/artists/{artistId}/availability/slots")
    public ResponseEntity<?> getSlots(@PathVariable Integer artistId,
                                       @RequestParam String data) {
        try {
            LocalDate localDate = LocalDate.parse(data);
            List<String> slots = disponibilidadeService.getSlotsDisponiveis(artistId, localDate);
            return ResponseEntity.ok(Map.of(
                    "artistId", artistId,
                    "data", data,
                    "slots", slots
            ));
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Formato de data invalido. Use YYYY-MM-DD."));
        }
    }

    /**
     * POST /api/v1/appointments
     * Processa o pedido de agendamento vindo da Landing Page.
     * Status forcado para "PENDENTE" — alimenta a aba Solicitacoes do Dashboard.
     *
     * Body esperado:
     * {
     *   "artistId": 1002,
     *   "clienteNome": "Joao Silva",
     *   "clienteEmail": "joao@email.com",
     *   "clienteTelefone": "(11) 99999-9999",
     *   "date": "2025-05-10",
     *   "time": "14:00",
     *   "description": "Tatuagem de lobo no antebraco",
     *   "estilo": "REALISTA",
     *   "regiao": "Antebraco",
     *   "largura": 10.0,
     *   "altura": 15.0,
     *   "tags": "Colorido,Design Personalizado",
     *   "imagemReferenciaUrl": "https://res.cloudinary.com/..."
     * }
     */
    @PostMapping("/appointments")
    public ResponseEntity<?> createAppointment(@RequestBody Map<String, Object> body) {
        // --- Validacoes basicas ---
        Object artistIdRaw = body.get("artistId");
        String clienteEmail = (String) body.get("clienteEmail");
        String clienteNome = (String) body.get("clienteNome");
        String date = (String) body.get("date");
        String time = (String) body.get("time");
        String description = (String) body.get("description");

        if (artistIdRaw == null || clienteEmail == null || date == null || description == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Campos obrigatorios: artistId, clienteEmail, date, description."));
        }

        Integer artistId = ((Number) artistIdRaw).intValue();

        // --- Resolve ou cria o cliente ---
        Cliente cliente = clienteService.getUserByEmail(clienteEmail)
                .orElseGet(() -> {
                    Cliente novo = new Cliente();
                    novo.setEmail(clienteEmail);
                    String username = clienteEmail.split("@")[0] + "_" + System.currentTimeMillis() % 10000;
                    novo.setUsername(username);
                    novo.setFullName(clienteNome != null ? clienteNome : clienteEmail.split("@")[0]);
                    novo.setTelefone((String) body.get("clienteTelefone"));
                    novo.setPassword("$2a$12$toQ38NJDsi349i1n.65MWuWFdALytJ2xzhzrOwq6JwyM6GyBINbIa");
                    return clienteService.saveCliente(novo);
                });

        // --- Resolve o artista ---
        Optional<Artista> artistaOpt = artistaService.getById(artistId);
        if (artistaOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Artista nao encontrado."));
        }

        // --- Monta dataHora ---
        LocalDateTime dataHora;
        try {
            String timeStr = time != null ? time : "12:00";
            dataHora = LocalDateTime.parse(date + "T" + timeStr + ":00");
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Formato de data/hora invalido. Use date: YYYY-MM-DD e time: HH:mm."));
        }

        // --- Monta o agendamento ---
        Agendamento ag = new Agendamento();
        ag.setCliente(cliente);
        ag.setArtista(artistaOpt.get());
        ag.setDataHora(dataHora);
        ag.setDescricao(description);
        ag.setStatus("PENDENTE"); // forcado — nunca confia no frontend
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
        resposta.put("message", "Solicitacao enviada com sucesso! O artista entrara em contato em breve.");
        resposta.put("dataHora", salvo.getDataHora().toString());
        resposta.put("artista", artistaOpt.get().getNome());

        return ResponseEntity.ok(resposta);
    }

    /**
     * GET /api/v1/appointments?artistId=1002
     * Lista agendamentos por artista para o Dashboard consumir via v1.
     * Requer autenticacao — alias do endpoint existente no dashboard.
     */
    @GetMapping("/appointments")
    public ResponseEntity<?> getAppointments(@RequestParam Integer artistId) {
        return ResponseEntity.ok(
                agendamentoService.getAgendamentosByArtistaId(artistId)
        );
    }
}
