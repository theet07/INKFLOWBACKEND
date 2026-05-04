package com.backend.INKFLOW.repository;

import com.backend.INKFLOW.model.CheckpointDia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CheckpointDiaRepository extends JpaRepository<CheckpointDia, Long> {

    List<CheckpointDia> findByCicatrizacaoIdOrderByNumeroDiaAsc(Long cicatrizacaoId);

    Optional<CheckpointDia> findByCicatrizacaoIdAndNumeroDia(Long cicatrizacaoId, Integer numeroDia);
}
