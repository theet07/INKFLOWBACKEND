package com.backend.INKFLOW.repository;

import com.backend.INKFLOW.model.BadgeUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface BadgeUsuarioRepository extends JpaRepository<BadgeUsuario, Long> {
    @Query("SELECT bu FROM BadgeUsuario bu JOIN FETCH bu.badge WHERE bu.cliente.id = :clienteId")
    List<BadgeUsuario> findByClienteIdWithBadge(@Param("clienteId") Long clienteId);

    List<BadgeUsuario> findByClienteId(Long clienteId);
    Optional<BadgeUsuario> findByClienteIdAndBadgeId(Long clienteId, Long badgeId);
}
