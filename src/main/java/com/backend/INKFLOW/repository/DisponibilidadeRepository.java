package com.backend.INKFLOW.repository;

import com.backend.INKFLOW.model.DisponibilidadeArtista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DisponibilidadeRepository extends JpaRepository<DisponibilidadeArtista, Long> {

    List<DisponibilidadeArtista> findByArtistaIdAndAtivoTrue(Integer artistaId);

    Optional<DisponibilidadeArtista> findByArtistaIdAndDiaSemana(Integer artistaId, Integer diaSemana);
}
