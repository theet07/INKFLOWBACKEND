package com.backend.INKFLOW.repository;

import com.backend.INKFLOW.model.SuspectLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SuspectLogRepository extends JpaRepository<SuspectLog, Long> {}
