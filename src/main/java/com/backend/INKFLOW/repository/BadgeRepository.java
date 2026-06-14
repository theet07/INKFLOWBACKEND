package com.backend.INKFLOW.repository;

import com.backend.INKFLOW.model.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BadgeRepository extends JpaRepository<Badge, Long> {
    List<Badge> findAllByOrderByCategoriaAscIdAsc();
}
