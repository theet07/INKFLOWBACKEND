package com.backend.INKFLOW.repository;

import com.backend.INKFLOW.model.BadgeUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BadgeUsuarioRepository extends JpaRepository<BadgeUsuario, Long> {
    List<BadgeUsuario> findByClienteId(Long clienteId);
    Optional<BadgeUsuario> findByClienteIdAndBadgeId(Long clienteId, Long badgeId);
}
