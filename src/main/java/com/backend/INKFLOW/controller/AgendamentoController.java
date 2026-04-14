package com.backend.INKFLOW.controller;

import com.backend.INKFLOW.model.Agendamento;
import com.backend.INKFLOW.model.AgendamentoDashboard;
import com.backend.INKFLOW.service.AgendamentoService;
import com.backend.INKFLOW.service.ArtistaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agendamentos")
public class AgendamentoController {

    @Autowired
    private AgendamentoService agendamentoService;

    @Autowired
    private ArtistaService artistaService;

    @GetMapping
    public List<Agendamento> getAllAgendamentos() {
        return agendamentoService.getAllAgendamentos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Agendamento> getAgendamentoById(@PathVariable Long id) {
        return agendamentoService.getAgendamentoById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cliente/{clienteId}")
    public List<Agendamento> getByCliente(@PathVariable Long clienteId) {
        return agendamentoService.getAgendamentosByClienteId(clienteId);
    }

    @GetMapping("/artista/{artistaId}")
    public ResponseEntity<?> getByArtista(@PathVariable Integer artistaId, Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            // Admin pode buscar qualquer artista pelo ID da URL
            List<AgendamentoDashboard> resultado = agendamentoService.getAgendamentosByArtistaId(artistaId)
                    .stream().map(AgendamentoDashboard::new).toList();
            return ResponseEntity.ok(resultado);
        }

        // Para ROLE_ARTISTA: ignora o ID da URL completamente.
        // Resolve o artista real pelo email extraido do token JWT.
        return artistaService.getByEmail(auth.getName())
                .map(artista -> {
                    // Valida que o ID da URL bate com o ID real do token — protecao extra
                    if (!artista.getId().equals(artistaId)) {
                        return ResponseEntity.status(403)
                                .<Object>body(Map.of("message", "Voce nao tem permissao para ver os agendamentos deste artista."));
                    }
                    // Busca usando o email do token, nunca o ID da URL
                    List<AgendamentoDashboard> resultado = agendamentoService
                            .getAgendamentosByArtistaEmail(auth.getName())
                            .stream().map(AgendamentoDashboard::new).toList();
                    return ResponseEntity.ok(resultado);
                })
                .orElse(ResponseEntity.status(403)
                        .body(Map.of("message", "Artista nao encontrado para este token.")));
    }

    @GetMapping("/status/{status}")
    public List<Agendamento> getAgendamentosByStatus(@PathVariable String status) {
        return agendamentoService.getAgendamentosByStatus(status);
    }

    @PostMapping
    public ResponseEntity<Agendamento> createAgendamento(@RequestBody Agendamento agendamento) {
        return ResponseEntity.ok(agendamentoService.saveAgendamento(agendamento));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
                                          @RequestBody Map<String, Object> body,
                                          Authentication auth) {
        String novoStatus = (String) body.get("status");
        Integer avaliacao = null;
        Object avaliacaoRaw = body.get("avaliacao");
        if (avaliacaoRaw instanceof Number) {
            avaliacao = ((Number) avaliacaoRaw).intValue();
        }
        String observacoes = (String) body.get("observacoes");

        boolean isCliente = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));

        if (isCliente) {
            // Busca o agendamento para validar
            Agendamento ag = agendamentoService.getAgendamentoById(id).orElse(null);
            if (ag == null) return ResponseEntity.notFound().build();

            // Ownership: email do token deve bater com o email do cliente do agendamento
            String emailToken = auth.getName();
            if (!emailToken.equals(ag.getCliente().getEmail())) {
                return ResponseEntity.status(403)
                        .body(Map.of("message", "Você não tem permissão para alterar este agendamento."));
            }

            // Cliente não pode marcar como REALIZADO
            if ("REALIZADO".equals(novoStatus)) {
                return ResponseEntity.status(403)
                        .body(Map.of("message", "Apenas o artista ou administrador pode marcar uma sessão como realizada."));
            }

            // Finalização: cliente pode mover de REALIZADO para FINALIZADO com avaliação obrigatória
            if ("FINALIZADO".equals(novoStatus)) {
                if (!"REALIZADO".equals(ag.getStatus())) {
                    return ResponseEntity.status(422)
                            .body(Map.of("message", "Só é possível finalizar uma sessão que já foi marcada como realizada pelo artista."));
                }
                if (avaliacao == null || avaliacao < 1 || avaliacao > 5) {
                    return ResponseEntity.status(422)
                            .body(Map.of("message", "Uma avaliação de 1 a 5 é obrigatória para finalizar a sessão."));
                }
            }

            // Cancelamento com menos de 24h de antecedência é bloqueado
            if ("CANCELADO".equals(novoStatus)) {
                long horasRestantes = ChronoUnit.HOURS.between(LocalDateTime.now(), ag.getDataHora());
                if (horasRestantes < 24) {
                    return ResponseEntity.status(422)
                            .body(Map.of("message", "Cancelamento não permitido. Faltam menos de 24 horas para a sessão. Entre em contato com o estúdio."));
                }
            }
        }

        return agendamentoService.updateStatus(id, novoStatus, avaliacao, observacoes)
                .map(updated -> (ResponseEntity<?>) ResponseEntity.ok(updated))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Agendamento> updateAgendamento(@PathVariable Long id, @RequestBody Agendamento agendamento) {
        return agendamentoService.getAgendamentoById(id)
                .map(existing -> {
                    agendamento.setId(id);
                    return ResponseEntity.ok(agendamentoService.saveAgendamento(agendamento));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAgendamento(@PathVariable Long id) {
        agendamentoService.deleteAgendamento(id);
        return ResponseEntity.ok().build();
    }
}
