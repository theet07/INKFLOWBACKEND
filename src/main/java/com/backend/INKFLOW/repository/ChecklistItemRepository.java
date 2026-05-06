package com.backend.INKFLOW.repository;

import com.backend.INKFLOW.model.ChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, Long> {

    List<ChecklistItem> findByCheckpointDiaIdOrderByPeriodoAscOrdemAsc(Long checkpointDiaId);

    @Query("SELECT c FROM ChecklistItem c JOIN FETCH c.checkpointDia cp JOIN FETCH cp.cicatrizacao WHERE c.id = :id")
    Optional<ChecklistItem> findByIdWithCheckpointAndCicatrizacao(@Param("id") Long id);
}
