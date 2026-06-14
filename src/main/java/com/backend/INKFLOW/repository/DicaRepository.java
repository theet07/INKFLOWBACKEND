package com.backend.INKFLOW.repository;

import com.backend.INKFLOW.model.Dica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface DicaRepository extends JpaRepository<Dica, Long> {
    @Query("SELECT d FROM Dica d WHERE d.diaInicio <= :dia AND d.diaFim >= :dia")
    List<Dica> findByDia(Integer dia);
}
