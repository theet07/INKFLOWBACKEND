package com.backend.INKFLOW.controller;

import com.backend.INKFLOW.dto.AgendamentoUpdateRequest;
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

    /** Retorna todos os agendamentos. Acesso restrito a ROLE_ADMIN. */
    @GetMapping
    public List<AgendamentoDashboard> getAllAgendamentos() {
        return agendamentoService.getAllAgendamentos()
                .stream().map(AgendamentoDashboard::new).toList();
    }

    /** Busca um agendamento pelo ID. */
    @GetMapping("/{id}")
    public ResponseEntity<?> getAgendamentoById(@PathVariable Long id, Authentication auth) {
        return agendamentoService.getAgendamentoById(id)
                .map(ag -> {
                    boolean isAdmin = auth.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                    boolean isArtista = ag.getArtista() != null &&
                            ag.getArtista().getEmail().equals(auth.getName());
                    boolean isCliente = ag.getCliente() != null &&
                            ag.getCliente().getEmail().equals(auth.getName());

                    if (!isAdmin && !isArtista && !isCliente)
                        return ResponseEntity.status(403)
                                .body(Map.of("message", "Acesso negado."));

                    return ResponseEntity.ok(new AgendamentoDashboard(ag));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** Retorna agendamentos de um cliente. Ownership: apenas o proprio cliente ou ADMIN. */
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<?> getByCliente(@PathVariable Long clienteId, Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        List<AgendamentoDashboard> resultado = agendamentoService
                .getAgendamentosByClienteId(clienteId)
                .stream().map(AgendamentoDashboard::new).toList();

        if (!isAdmin) {
            boolean isOwner = resultado.isEmpty() ||
                    resultado.get(0).getCliente() != null &&
                    resultado.get(0).getCliente().getEmail().equals(auth.getName());
            if (!isOwner)
                return ResponseEntity.status(403)
                        .body(Map.of("message", "Acesso negado."));
        }

        return ResponseEntity.ok(resultado);
    }

    /**
     * Retorna agendamentos do artista autenticado.
     * SEGURANCA: o artistaId da URL e usado apenas para validacao cruzada.
     * A query no banco usa exclusivamente o e-mail extraido do Token JWT,
     * prevenindo ataques IDOR (Insecure Direct Object Reference).
     */
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
    public ResponseEntity<?> getAgendamentosByStatus(@PathVariable String status, Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (isAdmin) {
            // Admin vê todos os agendamentos com esse status
            List<AgendamentoDashboard> resultado = agendamentoService.getAgendamentosByStatus(status)
                    .stream().map(AgendamentoDashboard::new).toList();
            return ResponseEntity.ok(resultado);
        }
        
        // Artista vê apenas seus próprios agendamentos
        return artistaService.getByEmail(auth.getName())
                .map(artista -> {
                    List<AgendamentoDashboard> resultado = agendamentoService
                            .getAgendamentosByArtistaEmail(auth.getName())
                            .stream()
                            .filter(ag -> status.equals(ag.getStatus()))
                            .map(AgendamentoDashboard::new)
                            .toList();
                    return ResponseEntity.<Object>ok(resultado);
                })
                .orElse(ResponseEntity.status(403)
                        .<Object>body(Map.of("message", "Acesso negado.")));
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

            // Cancelamento: apenas dentro de 24h após a criação do agendamento
            if ("CANCELADO".equals(novoStatus)) {
                long horasDesdeCriacao = ChronoUnit.HOURS.between(ag.getCreatedAt(), LocalDateTime.now());
                if (horasDesdeCriacao > 24) {
                    return ResponseEntity.status(422)
                            .body(Map.of("message", "Cancelamento não permitido. O prazo de 24h após o agendamento já expirou."));
                }
            }
        }

        return agendamentoService.updateStatus(id, novoStatus, avaliacao, observacoes)
                .map(updated -> (ResponseEntity<?>) ResponseEntity.ok(updated))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAgendamento(@PathVariable Long id,
                                                @RequestBody AgendamentoUpdateRequest request,
                                                Authentication auth) {
        return agendamentoService.getAgendamentoById(id)
                .map(existing -> {
                    boolean isAdmin = auth.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                    boolean isOwner = existing.getArtista() != null &&
                            existing.getArtista().getEmail().equals(auth.getName());
                    if (!isAdmin && !isOwner)
                        return ResponseEntity.status(403)
                                .body(Map.of("message", "Acesso negado."));
                    
                    // Atualiza apenas campos permitidos do DTO
                    if (request.getDataHora() != null) existing.setDataHora(request.getDataHora());
                    if (request.getServico() != null) existing.setServico(request.getServico());
                    if (request.getDescricao() != null) existing.setDescricao(request.getDescricao());
                    if (request.getRegiao() != null) existing.setRegiao(request.getRegiao());
                    if (request.getLargura() != null) existing.setLargura(request.getLargura());
                    if (request.getAltura() != null) existing.setAltura(request.getAltura());
                    if (request.getTags() != null) existing.setTags(request.getTags());
                    if (request.getImagemReferenciaUrl() != null) existing.setImagemReferenciaUrl(request.getImagemReferenciaUrl());
                    if (request.getImagemResultadoUrl() != null) existing.setImagemResultadoUrl(request.getImagemResultadoUrl());
                    if (request.getValorPago() != null) existing.setValorPago(request.getValorPago());
                    if (request.getValorPendente() != null) existing.setValorPendente(request.getValorPendente());
                    
                    return ResponseEntity.ok(
                            new AgendamentoDashboard(agendamentoService.saveAgendamento(existing)));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAgendamento(@PathVariable Long id) {
        agendamentoService.deleteAgendamento(id);
        return ResponseEntity.ok().build();
    }
}
