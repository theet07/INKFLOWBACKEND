package com.backend.INKFLOW.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Component
@Order(0)
public class SuspectLogTableInit implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SuspectLogTableInit.class);

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS suspect_logs (" +
                "id BIGSERIAL PRIMARY KEY, " +
                "ip VARCHAR(45), " +
                "mensagem VARCHAR(200), " +
                "timestamp TIMESTAMP)"
            );
            log.info("[Init] Tabela suspect_logs verificada/criada.");
        } catch (Exception e) {
            log.warn("[Init] Nao foi possivel criar tabela suspect_logs: {}", e.getMessage());
        }
    }
}
