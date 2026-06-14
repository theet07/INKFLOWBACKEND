package com.backend.INKFLOW.repository;

import com.backend.INKFLOW.model.CheckpointDia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface CheckpointDiaRepository extends JpaRepository<CheckpointDia, Long> {

    List<CheckpointDia> findByCicatrizacaoIdOrderByNumeroDiaAsc(Long cicatrizacaoId);

    Optional<CheckpointDia> findByCicatrizacaoIdAndNumeroDia(Long cicatrizacaoId, Integer numeroDia);

    @Query("SELECT c FROM CheckpointDia c JOIN FETCH c.cicatrizacao WHERE c.id = :id")
    Optional<CheckpointDia> findByIdWithCicatrizacao(@Param("id") Long id);
}
