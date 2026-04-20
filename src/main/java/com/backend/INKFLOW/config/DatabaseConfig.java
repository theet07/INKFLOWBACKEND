package com.backend.INKFLOW.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;

@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Bean
    @Primary
    public DataSource dataSource() {

        // ============================================================
        // ESTRATÉGIA 1: Variáveis separadas (DB_URL + DB_USERNAME + DB_PASSWORD)
        // ============================================================
        String dbUrl = getEnvOrNull("DB_URL");
        String dbUsername = getEnvOrNull("DB_USERNAME");
        String dbPassword = getEnvOrNull("DB_PASSWORD");

        if (dbUrl != null) {
            String driverClass = detectDriver(dbUrl);
            log.info("DataSource: DB_URL | driver={} | url={} | user={}",
                    driverClass, dbUrl, dbUsername != null ? dbUsername : "NULL");

            DataSourceBuilder<?> builder = DataSourceBuilder.create()
                    .url(dbUrl)
                    .driverClassName(driverClass);

            if (dbUsername != null) builder.username(dbUsername);
            if (dbPassword != null) builder.password(dbPassword);

            return builder.build();
        }

        // ============================================================
        // ESTRATÉGIA 2: DATABASE_URL combinada (formato postgres://user:pass@host:port/db)
        // ============================================================
        String databaseUrl = getEnvOrNull("DATABASE_URL");
        if (databaseUrl == null) databaseUrl = getEnvOrNull("DATABASE_INTERNAL_URL");

        if (databaseUrl != null) {
            log.info("DataSource: DATABASE_URL combinada");
            if (databaseUrl.startsWith("jdbc:")) {
                String driverClass = detectDriver(databaseUrl);
                log.info("DataSource: JDBC URL direta | driver={}", driverClass);
                return DataSourceBuilder.create()
                        .url(databaseUrl)
                        .driverClassName(driverClass)
                        .build();
            }

            // Parsing para postgres://USER:PASSWORD@HOST:PORT/DATABASE
            try {
                String url = databaseUrl;
                if (url.startsWith("postgres://")) {
                    url = url.substring("postgres://".length());
                } else if (url.startsWith("postgresql://")) {
                    url = url.substring("postgresql://".length());
                }

                int lastAtSign = url.lastIndexOf('@');
                if (lastAtSign < 0) {
                    throw new RuntimeException("DATABASE_URL sem '@'");
                }

                String credentials = url.substring(0, lastAtSign);
                String hostPortDb = url.substring(lastAtSign + 1);

                int colonIndex = credentials.indexOf(':');
                String username = colonIndex > 0 ? credentials.substring(0, colonIndex) : credentials;
                String password = colonIndex > 0 ? credentials.substring(colonIndex + 1) : "";

                String queryParams = null;
                int questionMark = hostPortDb.indexOf('?');
                if (questionMark > 0) {
                    queryParams = hostPortDb.substring(questionMark + 1);
                    hostPortDb = hostPortDb.substring(0, questionMark);
                }

                StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://");
                jdbcUrl.append(hostPortDb);
                if (queryParams != null) {
                    jdbcUrl.append("?").append(queryParams);
                    if (!queryParams.contains("sslmode")) jdbcUrl.append("&sslmode=require");
                } else {
                    jdbcUrl.append("?sslmode=require");
                }

                log.info("DataSource: JDBC URL construida={}", jdbcUrl);
                return DataSourceBuilder.create()
                        .url(jdbcUrl.toString())
                        .username(username)
                        .password(password)
                        .driverClassName("org.postgresql.Driver")
                        .build();

            } catch (Exception e) {
                log.error("Falha ao parsear DATABASE_URL: {}", e.getMessage());
                throw new RuntimeException("Falha ao configurar DataSource", e);
            }
        }

        // ============================================================
        // FALLBACK: localhost
        // ============================================================
        log.warn("Nenhuma variavel de banco encontrada. Usando localhost como fallback.");
        return DataSourceBuilder.create()
                .url("jdbc:postgresql://localhost:5432/inkflow")
                .username("postgres")
                .password("postgres")
                .driverClassName("org.postgresql.Driver")
                .build();
    }

    /**
     * Detecta o driver correto baseado na URL JDBC
     */
    private String detectDriver(String jdbcUrl) {
        if (jdbcUrl.contains("sqlserver")) {
            return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        } else if (jdbcUrl.contains("postgresql") || jdbcUrl.contains("postgres")) {
            return "org.postgresql.Driver";
        } else {
            // Tentar SQL Server como default para somee.com
            return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        }
    }

    private String getEnvOrNull(String name) {
        String value = System.getenv(name);
        if (value != null) {
            value = value.trim();
            if (value.isEmpty()) return null;
        }
        return value;
    }
}
