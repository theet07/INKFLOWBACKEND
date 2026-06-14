package com.backend.INKFLOW.controller;

import com.backend.INKFLOW.model.CheckpointDia;
import com.backend.INKFLOW.model.Cicatrizacao;
import com.backend.INKFLOW.repository.CheckpointDiaRepository;
import com.backend.INKFLOW.repository.CicatrizacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/estatisticas")
public class EstatisticasController {

    @Autowired
    private CicatrizacaoRepository cicatrizacaoRepository;

    @Autowired
    private CheckpointDiaRepository checkpointDiaRepository;

    /** GET /api/estatisticas/cicatrizacao/{cicatrizacaoId} */
    @GetMapping("/cicatrizacao/{cicatrizacaoId}")
    public ResponseEntity<?> getEstatisticas(@PathVariable Long cicatrizacaoId) {
        Optional<Cicatrizacao> cicOpt = cicatrizacaoRepository.findById(cicatrizacaoId);
        if (cicOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Cicatrizacao cic = cicOpt.get();
        List<CheckpointDia> dias = checkpointDiaRepository.findByCicatrizacaoIdOrderByNumeroDiaAsc(cicatrizacaoId);

        // XP por dia
        List<Map<String, Object>> xpPorDia = new ArrayList<>();
        for (CheckpointDia d : dias) {
            if (!"BLOQUEADO".equals(d.getStatusDia())) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("dia", d.getNumeroDia());
                entry.put("xp", d.getXpGanho());
                xpPorDia.add(entry);
            }
        }

        // Streak atual
        int streakAtual = 0;
        for (int i = dias.size() - 1; i >= 0; i--) {
            String status = dias.get(i).getStatusDia();
            if ("COMPLETO".equals(status)) {
                streakAtual++;
            } else if ("DISPONIVEL".equals(status) || "BLOQUEADO".equals(status)) {
                continue; // pular dias futuros
            } else {
                break;
            }
        }

        // Melhor streak
        int melhorStreak = 0, currentStreak = 0;
        for (CheckpointDia d : dias) {
            if ("COMPLETO".equals(d.getStatusDia())) {
                currentStreak++;
                melhorStreak = Math.max(melhorStreak, currentStreak);
            } else {
                currentStreak = 0;
            }
        }

        long diasCompletos = dias.stream().filter(d -> "COMPLETO".equals(d.getStatusDia())).count();
        int totalDias = cic.getPeriodoTotalDias();
        int taxaConclusao = totalDias > 0 ? (int) (diasCompletos * 100 / totalDias) : 0;

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("xpPorDia", xpPorDia);
        resultado.put("streakAtual", streakAtual);
        resultado.put("melhorStreak", melhorStreak);
        resultado.put("diasCompletos", diasCompletos);
        resultado.put("totalDias", totalDias);
        resultado.put("taxaConclusao", taxaConclusao);

        return ResponseEntity.ok(resultado);
    }
}
