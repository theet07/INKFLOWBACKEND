package com.backend.INKFLOW.repository;

import com.backend.INKFLOW.model.FotoEvolucao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FotoEvolucaoRepository extends JpaRepository<FotoEvolucao, Long> {
    List<FotoEvolucao> findByCicatrizacaoIdOrderByNumeroDiaAsc(Long cicatrizacaoId);
}
