package com.backend.INKFLOW.repository;

import com.backend.INKFLOW.model.NotificacaoPreferencia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NotificacaoPreferenciaRepository extends JpaRepository<NotificacaoPreferencia, Long> {
    Optional<NotificacaoPreferencia> findByClienteId(Long clienteId);
}
