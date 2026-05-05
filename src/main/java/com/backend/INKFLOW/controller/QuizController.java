package com.backend.INKFLOW.controller;

import com.backend.INKFLOW.model.QuizOpcao;
import com.backend.INKFLOW.model.QuizPergunta;
import com.backend.INKFLOW.repository.QuizOpcaoRepository;
import com.backend.INKFLOW.repository.QuizPerguntaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    @Autowired
    private QuizPerguntaRepository perguntaRepository;

    @Autowired
    private QuizOpcaoRepository opcaoRepository;

    /** GET /api/quiz/dia/{diaNumero} */
    @GetMapping("/dia/{diaNumero}")
    public ResponseEntity<List<Map<String, Object>>> getQuiz(@PathVariable Integer diaNumero) {
        List<QuizPergunta> perguntas = perguntaRepository.findByCheckpointDiaNumero(diaNumero);

        List<Map<String, Object>> resultado = new ArrayList<>();
        for (QuizPergunta p : perguntas) {
            List<QuizOpcao> opcoes = opcaoRepository.findByPerguntaIdOrderByIndiceAsc(p.getId());

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", p.getId());
            item.put("pergunta", p.getPergunta());
            item.put("opcoes", opcoes.stream().map(QuizOpcao::getTexto).toList());
            item.put("respostaCorreta", p.getRespostaCorreta());
            item.put("explicacao", p.getExplicacao());
            item.put("xpBonus", p.getXpBonus());
            resultado.add(item);
        }

        return ResponseEntity.ok(resultado);
    }

    /** POST /api/quiz/responder */
    @PostMapping("/responder")
    public ResponseEntity<Map<String, Object>> responder(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        Map<String, Object> respostasRaw = (Map<String, Object>) body.get("respostas");

        int acertos = 0;
        int xpGanho = 0;
        int totalPerguntas = 0;

        if (respostasRaw != null) {
            for (Map.Entry<String, Object> entry : respostasRaw.entrySet()) {
                Long perguntaId = Long.parseLong(entry.getKey());
                int respostaUsuario = ((Number) entry.getValue()).intValue();

                Optional<QuizPergunta> pOpt = perguntaRepository.findById(perguntaId);
                if (pOpt.isPresent()) {
                    totalPerguntas++;
                    if (pOpt.get().getRespostaCorreta() == respostaUsuario) {
                        acertos++;
                        xpGanho += pOpt.get().getXpBonus();
                    }
                }
            }
        }

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("acertos", acertos);
        resultado.put("totalPerguntas", totalPerguntas);
        resultado.put("xpGanho", xpGanho);
        resultado.put("percentualAcerto", totalPerguntas > 0 ? (acertos * 100 / totalPerguntas) : 0);

        return ResponseEntity.ok(resultado);
    }
}
