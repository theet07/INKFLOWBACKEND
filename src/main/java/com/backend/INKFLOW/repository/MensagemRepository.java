package com.backend.INKFLOW.repository;

import com.backend.INKFLOW.model.Mensagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MensagemRepository extends JpaRepository<Mensagem, Long> {

    @Query("SELECT m FROM Mensagem m WHERE " +
           "(m.remetenteId = :a AND m.destinatarioId = :b) OR " +
           "(m.remetenteId = :b AND m.destinatarioId = :a) " +
           "ORDER BY m.createdAt ASC")
    List<Mensagem> findConversa(@Param("a") Long a, @Param("b") Long b);

    @Query("SELECT m FROM Mensagem m WHERE m.destinatarioId = :userId AND m.createdAt > :desde ORDER BY m.createdAt ASC")
    List<Mensagem> findNovas(@Param("userId") Long userId, @Param("desde") LocalDateTime desde);

    long countByDestinatarioIdAndLidaFalse(Long destinatarioId);

    List<Mensagem> findByDestinatarioIdAndLidaFalse(Long destinatarioId);

    @Query("SELECT DISTINCT m.remetenteId FROM Mensagem m WHERE m.destinatarioId = :artistaId")
    List<Long> findRemetentesDoArtista(@Param("artistaId") Long artistaId);
}
