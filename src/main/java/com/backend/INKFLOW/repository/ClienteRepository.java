package com.backend.INKFLOW.repository;

import com.backend.INKFLOW.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByUsername(String username);
    Optional<Cliente> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Modifying
    @Transactional
    @Query("DELETE FROM Cliente c WHERE c.contaVerificada = false AND c.createdAt < :limite")
    int deleteNaoVerificadosAntesDe(@Param("limite") LocalDateTime limite);
}