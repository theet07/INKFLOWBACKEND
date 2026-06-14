package com.backend.INKFLOW.controller;

import com.backend.INKFLOW.model.*;
import com.backend.INKFLOW.dto.*;
import com.backend.INKFLOW.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/badges")
public class BadgeController {

    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private BadgeUsuarioRepository badgeUsuarioRepository;

    @Autowired
    private CheckpointDiaRepository checkpointDiaRepository;

    @Autowired
    private CicatrizacaoRepository cicatrizacaoRepository;

    /** GET /api/badges/usuario/{usuarioId} */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Map<String, Object>>> getBadgesUsuario(@PathVariable Long usuarioId) {
        List<Badge> todasBadges = badgeRepository.findAllByOrderByCategoriaAscIdAsc();
        List<BadgeUsuario> badgesUsuario = badgeUsuarioRepository.findByClienteIdWithBadge(usuarioId);

        Map<Long, BadgeUsuario> mapBU = badgesUsuario.stream()
                .collect(Collectors.toMap(bu -> bu.getBadge().getId(), bu -> bu));

        List<Map<String, Object>> resultado = new ArrayList<>();

        for (Badge badge : todasBadges) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", badge.getId());
            item.put("nome", badge.getNome());
            item.put("descricao", badge.getDescricao());
            item.put("icone", badge.getIcone());
            item.put("categoria", badge.getCategoria());

            BadgeUsuario bu = mapBU.get(badge.getId());
            if (bu != null) {
                item.put("desbloqueado", bu.getDesbloqueado());
                item.put("dataDesbloqueio", bu.getDataDesbloqueio());
                item.put("progresso", bu.getProgresso());
            } else {
                item.put("desbloqueado", false);
                item.put("dataDesbloqueio", null);
                item.put("progresso", calcularProgresso(badge, usuarioId));
            }

            resultado.add(item);
        }

        return ResponseEntity.ok(resultado);
    }

    private int calcularProgresso(Badge badge, Long clienteId) {
        // Buscar cicatrização ativa do cliente para calcular progresso
        try {
            var cicOpt = cicatrizacaoRepository.findFirstByAgendamentoClienteIdAndStatus(clienteId, "ATIVA");
            if (cicOpt.isEmpty()) return 0;

            Cicatrizacao cic = cicOpt.get();
            List<CheckpointDia> dias = checkpointDiaRepository.findByCicatrizacaoIdOrderByNumeroDiaAsc(cic.getId());
            long diasCompletos = dias.stream().filter(d -> "COMPLETO".equals(d.getStatusDia())).count();

            switch (badge.getCriterioTipo()) {
                case "STREAK_DIAS":
                    int streak = calcularStreak(dias);
                    return Math.min(100, (streak * 100) / badge.getCriterioValor());
                case "DIAS_COMPLETOS":
                    return Math.min(100, (int) (diasCompletos * 100) / badge.getCriterioValor());
                case "XP_TOTAL":
                    return Math.min(100, (cic.getXpTotal() * 100) / badge.getCriterioValor());
                case "CICATRIZACAO_TOTAL":
                    return Math.min(100, 33); // simplificado
                default:
                    return 0;
            }
        } catch (Exception e) {
            return 0;
        }
    }

    private int calcularStreak(List<CheckpointDia> dias) {
        int streak = 0;
        for (int i = dias.size() - 1; i >= 0; i--) {
            if ("COMPLETO".equals(dias.get(i).getStatusDia())) {
                streak++;
            } else if ("DISPONIVEL".equals(dias.get(i).getStatusDia()) || "BLOQUEADO".equals(dias.get(i).getStatusDia())) {
                break; // dias futuros, ignorar
            } else {
                break; // streak quebrado
            }
        }
        return streak;
    }
}
