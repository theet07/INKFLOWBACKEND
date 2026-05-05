package com.backend.INKFLOW.repository;

import com.backend.INKFLOW.model.QuizPergunta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuizPerguntaRepository extends JpaRepository<QuizPergunta, Long> {
    List<QuizPergunta> findByCheckpointDiaNumero(Integer diaNumero);
}
