package com.backend.INKFLOW.controller;

import com.backend.INKFLOW.model.DisponibilidadeArtista;
import com.backend.INKFLOW.service.ArtistaService;
import com.backend.INKFLOW.service.DisponibilidadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/disponibilidade")
public class DisponibilidadeController {

    @Autowired
    private DisponibilidadeService disponibilidadeService;

    @Autowired
    private ArtistaService artistaService;

    /**
     * Retorna a grade semanal de disponibilidade de um artista.
     * Publico — usado pelo formulario de agendamento do cliente.
     */
    @GetMapping("/artista/{artistaId}")
    public List<DisponibilidadeArtista> getByArtista(@PathVariable Integer artistaId) {
        return disponibilidadeService.getByArtista(artistaId);
    }

    /**
     * Retorna os slots de horario disponivel para um artista em uma data.
     * Publico — alimenta o calendario dinamico do frontend.
     *
     * Exemplo: GET /api/disponibilidade/artista/1002/slots?data=2025-05-10
     */
    @GetMapping("/artista/{artistaId}/slots")
    public ResponseEntity<?> getSlots(@PathVariable Integer artistaId,
                                       @RequestParam String data) {
        try {
            LocalDate localDate = LocalDate.parse(data);
            List<String> slots = disponibilidadeService.getSlotsDisponiveis(artistaId, localDate);
            return ResponseEntity.ok(Map.of("data", data, "artistaId", artistaId, "slots", slots));
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Formato de data invalido. Use YYYY-MM-DD."));
        }
    }

    /**
     * Retorna o calendario mensal completo com slots disponiveis por dia.
     * Publico — usado pelo calendario do mes no frontend.
     *
     * Exemplo: GET /api/disponibilidade/artista/1002/calendario?ano=2025&mes=5
     */
    @GetMapping("/artista/{artistaId}/calendario")
    public ResponseEntity<?> getCalendario(@PathVariable Integer artistaId,
                                            @RequestParam int ano,
                                            @RequestParam int mes) {
        if (mes < 1 || mes > 12) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mes invalido."));
        }
        Map<String, List<String>> calendario =
                disponibilidadeService.getCalendarioMensal(artistaId, ano, mes);
        return ResponseEntity.ok(Map.of("artistaId", artistaId, "ano", ano, "mes", mes,
                                        "disponibilidade", calendario));
    }

    /**
     * Salva ou atualiza a disponibilidade de um dia da semana.
     * Restrito ao proprio artista ou ADMIN.
     */
    @PostMapping("/artista/{artistaId}")
    public ResponseEntity<?> salvar(@PathVariable Integer artistaId,
                                     @RequestBody Map<String, Object> body,
                                     Authentication auth) {
        if (!isOwnerOrAdmin(artistaId, auth)) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Voce nao tem permissao para alterar esta disponibilidade."));
        }

        Object diaRaw = body.get("diaSemana");
        if (!(diaRaw instanceof Number)) {
            return ResponseEntity.badRequest().body(Map.of("message", "diaSemana e obrigatorio (0=Seg, 6=Dom)."));
        }

        Integer diaSemana = ((Number) diaRaw).intValue();
        String horaInicio = (String) body.get("horaInicio");
        String horaFim = (String) body.get("horaFim");
        Object slotRaw = body.get("duracaoSlotMinutos");
        Integer duracaoSlot = slotRaw instanceof Number ? ((Number) slotRaw).intValue() : 60;

        if (horaInicio == null || horaFim == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "horaInicio e horaFim sao obrigatorios."));
        }

        return disponibilidadeService.salvar(artistaId, diaSemana, horaInicio, horaFim, duracaoSlot)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** Remove (desativa) um registro de disponibilidade. Restrito ao artista ou ADMIN. */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> remover(@PathVariable Long id, Authentication auth) {
        disponibilidadeService.remover(id);
        return ResponseEntity.ok().build();
    }

    private boolean isOwnerOrAdmin(Integer artistaId, Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) return true;
        return artistaService.getByEmail(auth.getName())
                .map(a -> a.getId().equals(artistaId))
                .orElse(false);
    }
}
