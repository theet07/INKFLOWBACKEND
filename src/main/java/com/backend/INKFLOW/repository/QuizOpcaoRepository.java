package com.backend.INKFLOW.repository;

import com.backend.INKFLOW.model.QuizOpcao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuizOpcaoRepository extends JpaRepository<QuizOpcao, Long> {
    List<QuizOpcao> findByPerguntaIdOrderByIndiceAsc(Long perguntaId);
}
