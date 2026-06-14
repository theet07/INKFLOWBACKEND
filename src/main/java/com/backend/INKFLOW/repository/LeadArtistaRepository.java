package com.backend.INKFLOW.repository;

import com.backend.INKFLOW.model.LeadArtista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeadArtistaRepository extends JpaRepository<LeadArtista, Long> {
    Optional<LeadArtista> findByWhatsapp(String whatsapp);
}
