package com.backend.INKFLOW.controller;

import com.backend.INKFLOW.model.Agendamento;
import com.backend.INKFLOW.service.AgendamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agendamentos")
public class AgendamentoController {

    @Autowired
    private AgendamentoService agendamentoService;

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
    public List<Agendamento> getByArtista(@PathVariable Integer artistaId) {
        return agendamentoService.getAgendamentosByArtistaId(artistaId);
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
    public ResponseEntity<Agendamento> updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String status = (String) body.get("status");
        Integer avaliacao = body.get("avaliacao") != null ? (Integer) body.get("avaliacao") : null;
        String observacoes = (String) body.get("observacoes");
        return agendamentoService.updateStatus(id, status, avaliacao, observacoes)
                .map(ResponseEntity::ok)
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
