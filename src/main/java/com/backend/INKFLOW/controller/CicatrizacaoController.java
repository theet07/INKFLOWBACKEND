package com.backend.INKFLOW.controller;

import com.backend.INKFLOW.model.ChecklistItem;
import com.backend.INKFLOW.model.CheckpointDia;
import com.backend.INKFLOW.model.Cicatrizacao;
import com.backend.INKFLOW.service.CicatrizacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cicatrizacao")
public class CicatrizacaoController {

    @Autowired
    private CicatrizacaoService cicatrizacaoService;

    /** Busca a cicatrização ativa do cliente */
    @GetMapping("/ativa/{clienteId}")
    public ResponseEntity<?> buscarAtiva(@PathVariable Long clienteId, Authentication auth) {
        return cicatrizacaoService.buscarAtiva(clienteId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    /** Retorna todos os checkpoints do caminho (tela Duolingo) */
    @GetMapping("/{id}/caminho")
    public ResponseEntity<List<CheckpointDia>> buscarCaminho(@PathVariable Long id) {
        return ResponseEntity.ok(cicatrizacaoService.buscarCaminho(id));
    }

    /** Retorna o checklist de um dia específico */
    @GetMapping("/{id}/checklist/dia/{numeroDia}")
    public ResponseEntity<List<ChecklistItem>> buscarChecklistDia(
            @PathVariable Long id,
            @PathVariable Integer numeroDia) {
        return ResponseEntity.ok(cicatrizacaoService.buscarChecklistDia(id, numeroDia));
    }

    /** Marca/desmarca um item do checklist */
    @PatchMapping("/{id}/checklist/{itemId}/toggle")
    public ResponseEntity<CheckpointDia> toggleItem(
            @PathVariable Long id,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(cicatrizacaoService.toggleItem(id, itemId));
    }

    /** Endpoint manual para iniciar cicatrização (admin/teste) */
    @PostMapping("/iniciar/{agendamentoId}")
    public ResponseEntity<?> iniciar(@PathVariable Long agendamentoId, Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            return ResponseEntity.status(403).body(Map.of("message", "Acesso negado."));
        }
        return ResponseEntity.ok(cicatrizacaoService.iniciar(agendamentoId));
    }
}
