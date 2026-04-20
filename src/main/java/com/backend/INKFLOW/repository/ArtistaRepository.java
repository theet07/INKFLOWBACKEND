package com.backend.INKFLOW.repository;

import com.backend.INKFLOW.model.Artista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArtistaRepository extends JpaRepository<Artista, Integer> {
    List<Artista> findByAtivoTrue();
    Optional<Artista> findByEmail(String email);
}
