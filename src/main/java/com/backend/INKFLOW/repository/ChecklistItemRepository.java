package com.backend.INKFLOW.repository;

import com.backend.INKFLOW.model.ChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, Long> {

    List<ChecklistItem> findByCheckpointDiaIdOrderByPeriodoAscOrdemAsc(Long checkpointDiaId);
}
