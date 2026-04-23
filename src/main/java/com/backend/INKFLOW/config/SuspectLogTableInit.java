package com.backend.INKFLOW.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(0)
public class SuspectLogTableInit implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SuspectLogTableInit.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) {
        try {
            entityManager.createNativeQuery(
                "CREATE TABLE IF NOT EXISTS suspect_logs (" +
                "id BIGSERIAL PRIMARY KEY, " +
                "ip VARCHAR(45), " +
                "mensagem VARCHAR(200), " +
                "timestamp TIMESTAMP" +
                ")"
            ).executeUpdate();
            log.info("[Init] Tabela suspect_logs verificada/criada.");
        } catch (Exception e) {
            log.warn("[Init] Nao foi possivel criar tabela suspect_logs: {}", e.getMessage());
        }
    }
}
