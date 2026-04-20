package com.backend.INKFLOW.repository;

import com.backend.INKFLOW.model.PortfolioItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PortfolioRepository extends JpaRepository<PortfolioItem, Long> {
    List<PortfolioItem> findByArtistaIdOrderByIdDesc(Integer artistaId);
}
