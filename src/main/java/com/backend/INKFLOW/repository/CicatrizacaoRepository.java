package com.backend.INKFLOW.repository;

import com.backend.INKFLOW.model.Cicatrizacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface CicatrizacaoRepository extends JpaRepository<Cicatrizacao, Long> {

    @Query("SELECT c FROM Cicatrizacao c WHERE c.agendamento.cliente.id = :clienteId AND c.status = 'ATIVA'")
    Optional<Cicatrizacao> findAtivaByClienteId(@Param("clienteId") Long clienteId);

    Optional<Cicatrizacao> findByAgendamentoId(Long agendamentoId);
}
