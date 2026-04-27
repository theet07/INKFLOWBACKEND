package com.backend.INKFLOW.controller;

import com.backend.INKFLOW.service.AgendamentoService;
import com.backend.INKFLOW.service.ArtistaService;
import com.backend.INKFLOW.service.DisponibilidadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller v1 dedicado ao fluxo de captura da Landing Page "Para Tatuadores".
 * Todos os endpoints sao publicos — nao requerem token JWT.
 * Nao altera nenhuma rota do dashboard existente.
 */
@RestController
@RequestMapping("/api/v1")
public class LandingPageController {

    @Autowired private DisponibilidadeService disponibilidadeService;
    @Autowired private AgendamentoService agendamentoService;
    @Autowired private ArtistaService artistaService;

    /**
     * GET /api/v1/artists/{artistId}/availability?ano=2025&mes=5
     * Retorna o calendario mensal com slots disponiveis por dia.
     * Alimenta o calendario dinamico que o cliente ve na Landing Page.
     */
    @GetMapping("/artists/{artistId}/availability")
    public ResponseEntity<?> getAvailability(@PathVariable Integer artistId,
                                              @RequestParam(required = false) Integer ano,
                                              @RequestParam(required = false) Integer mes) {
        try {
            if (artistaService.getById(artistId).isEmpty())
                return ResponseEntity.notFound().build();

            int anoParam = ano != null ? ano : LocalDate.now().getYear();
            int mesParam = mes != null ? mes : LocalDate.now().getMonthValue();

            if (mesParam < 1 || mesParam > 12)
                return ResponseEntity.badRequest().body(Map.of("message", "Mes invalido."));

            List<Map<String, Object>> dias = disponibilidadeService
                    .getCalendarioMensal(artistId, anoParam, mesParam)
                    .entrySet().stream()
                    .map(e -> {
                        Map<String, Object> dia = new HashMap<>();
                        dia.put("data", e.getKey());
                        dia.put("disponivel", !e.getValue().isEmpty());
                        dia.put("slots", e.getValue());
                        return dia;
                    })
                    .collect(java.util.stream.Collectors.toList());

            return ResponseEntity.ok(dias);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Erro ao carregar disponibilidade."));
        }
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
            // Converte para [{horario, disponivel}] esperado pelo Booking.jsx
            List<Map<String, Object>> resultado = slots.stream()
                    .map(s -> {
                        Map<String, Object> slot = new HashMap<>();
                        slot.put("horario", s);
                        slot.put("disponivel", true);
                        return slot;
                    })
                    .collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(resultado);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Formato de data invalido. Use YYYY-MM-DD."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Erro ao carregar slots."));
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
        try {
            var salvo = agendamentoService.criarAgendamentoLandingPage(body);
            Map<String, Object> resposta = new HashMap<>();
            resposta.put("success", true);
            resposta.put("id", salvo.getId());
            resposta.put("status", salvo.getStatus());
            resposta.put("message", "Solicitacao enviada com sucesso! O artista entrara em contato em breve.");
            resposta.put("dataHora", salvo.getDataHora().toString());
            return ResponseEntity.ok(resposta);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", e.getReason()));
        }
    }

    /**
     * GET /api/v1/appointments?artistId=1002
     * Lista agendamentos por artista para o Dashboard consumir via v1.
     * Requer autenticacao — alias do endpoint existente no dashboard.
     */
    @GetMapping("/appointments")
    public ResponseEntity<?> getAppointments(@RequestParam Integer artistId, Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            // Artista so pode ver os proprios agendamentos
            boolean isOwner = artistaService.getByEmail(auth.getName())
                    .map(a -> a.getId().equals(artistId))
                    .orElse(false);
            if (!isOwner) {
                return ResponseEntity.status(403)
                        .body(Map.of("message", "Acesso negado."));
            }
        }
        return ResponseEntity.ok(
                agendamentoService.getAgendamentosByArtistaId(artistId)
        );
    }
}
