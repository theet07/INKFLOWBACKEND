package com.backend.INKFLOW.repository;

import com.backend.INKFLOW.model.Agendamento;
import com.backend.INKFLOW.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {
    List<Agendamento> findByCliente(Cliente cliente);
    List<Agendamento> findByClienteId(Long clienteId);
    List<Agendamento> findByArtistaId(Integer artistaId);
    List<Agendamento> findByArtistaIdOrderByDataHoraAsc(Integer artistaId);
    List<Agendamento> findByStatus(String status);
    List<Agendamento> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);
    List<Agendamento> findAllByOrderByDataHoraAsc();
    
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("DELETE FROM Agendamento a WHERE a.cliente.id = :clienteId")
    void deleteByClienteId(@Param("clienteId") Long clienteId);
}
