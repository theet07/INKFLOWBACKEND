package com.backend.INKFLOW.controller;

import com.backend.INKFLOW.model.AgendamentoDashboard;
import com.backend.INKFLOW.service.AgendamentoService;
import com.backend.INKFLOW.service.ClienteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private static final Logger log = LoggerFactory.getLogger(AppointmentController.class);

    @Autowired private AgendamentoService agendamentoService;
    @Autowired private ClienteService clienteService;

    /** POST /api/appointments — aceita payload do Booking.jsx ou da Landing Page */
    @PostMapping
    public ResponseEntity<?> createAppointment(@RequestBody Map<String, Object> body) {
        try {
            boolean isDireto = body.get("artista") instanceof Map && body.get("cliente") instanceof Map;
            var salvo = isDireto
                    ? agendamentoService.criarAgendamentoDireto(body)
                    : agendamentoService.criarAgendamentoLandingPage(body);
            return ResponseEntity.status(201).body(Map.of(
                    "success", true,
                    "id", salvo.getId(),
                    "status", salvo.getStatus(),
                    "message", "Agendamento criado com sucesso!"));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", e.getReason()));
        } catch (Exception e) {
            log.error("[Appointment] Erro inesperado: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("message", "Erro ao criar agendamento."));
        }
    }

    /** GET /api/appointments/meus — agendamentos do cliente autenticado via JWT */
    @GetMapping("/meus")
    public ResponseEntity<?> getMeusAgendamentos(Authentication auth) {
        return clienteService.getUserByEmail(auth.getName())
                .map(c -> ResponseEntity.ok(
                        agendamentoService.getAgendamentosByClienteId(c.getId())
                                .stream().map(AgendamentoDashboard::new).toList()))
                .orElse(ResponseEntity.notFound().build());
    }

    /** GET /api/appointments/cliente/{clienteId} — para admin ou owner */
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<?> getByCliente(@PathVariable Long clienteId, Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            boolean isOwner = agendamentoService.getAgendamentosByClienteId(clienteId)
                    .stream().findFirst()
                    .map(ag -> ag.getCliente().getEmail().equals(auth.getName()))
                    .orElse(true);
            if (!isOwner)
                return ResponseEntity.status(403).body(Map.of("message", "Acesso negado."));
        }
        return ResponseEntity.ok(
                agendamentoService.getAgendamentosByClienteId(clienteId)
                        .stream().map(AgendamentoDashboard::new).toList());
    }

    /** PUT /api/appointments/{id}/avaliar — apenas o cliente dono pode avaliar */
    @PutMapping("/{id}/avaliar")
    public ResponseEntity<?> avaliar(@PathVariable Long id,
                                      @RequestBody Map<String, Object> body,
                                      Authentication auth) {
        return agendamentoService.getAgendamentoById(id)
                .map(ag -> {
                    if (!ag.getCliente().getEmail().equals(auth.getName()))
                        return ResponseEntity.status(403).body(Map.of("message", "Acesso negado."));
                    Integer nota = body.get("avaliacao") instanceof Number
                            ? ((Number) body.get("avaliacao")).intValue() : null;
                    if (nota == null || nota < 1 || nota > 5)
                        return ResponseEntity.badRequest().body(Map.of("message", "Avaliacao deve ser entre 1 e 5."));
                    return agendamentoService.avaliar(id, nota, (String) body.get("observacoes"))
                            .map(salvo -> (ResponseEntity<?>) ResponseEntity.ok(salvo))
                            .orElse(ResponseEntity.notFound().build());
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
