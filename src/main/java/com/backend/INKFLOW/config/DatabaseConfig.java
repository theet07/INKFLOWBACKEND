package com.backend.INKFLOW.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() {

        // ============================================================
        // ESTRATÉGIA 1: Variáveis separadas (DB_URL + DB_USERNAME + DB_PASSWORD)
        // Essas são as que existem no Render do INKFLOW
        // ============================================================
        String dbUrl = getEnvOrNull("DB_URL");
        String dbUsername = getEnvOrNull("DB_USERNAME");
        String dbPassword = getEnvOrNull("DB_PASSWORD");

        if (dbUrl != null) {
            System.out.println(">>> Usando DB_URL + DB_USERNAME + DB_PASSWORD");

            // Garantir que a URL começa com jdbc:postgresql://
            if (!dbUrl.startsWith("jdbc:")) {
                if (dbUrl.startsWith("postgres://")) {
                    dbUrl = "jdbc:postgresql://" + dbUrl.substring("postgres://".length());
                } else if (dbUrl.startsWith("postgresql://")) {
                    dbUrl = "jdbc:" + dbUrl;
                } else {
                    // Assumir que é host:port/db
                    dbUrl = "jdbc:postgresql://" + dbUrl;
                }
            }

            // Adicionar sslmode se não presente
            if (!dbUrl.contains("sslmode")) {
                dbUrl += (dbUrl.contains("?") ? "&" : "?") + "sslmode=require";
            }

            System.out.println(">>> JDBC URL: " + dbUrl);
            System.out.println(">>> Username: " + (dbUsername != null ? dbUsername : "NULL"));

            DataSourceBuilder<?> builder = DataSourceBuilder.create()
                    .url(dbUrl)
                    .driverClassName("org.postgresql.Driver");

            if (dbUsername != null) builder.username(dbUsername);
            if (dbPassword != null) builder.password(dbPassword);

            return builder.build();
        }

        // ============================================================
        // ESTRATÉGIA 2: URL combinada (DATABASE_URL no formato postgres://user:pass@host:port/db)
        // ============================================================
        String databaseUrl = getEnvOrNull("DATABASE_URL");
        if (databaseUrl == null) databaseUrl = getEnvOrNull("DATABASE_INTERNAL_URL");
        if (databaseUrl == null) databaseUrl = getEnvOrNull("DATABASE_EXTERNAL_URL");
        if (databaseUrl == null) databaseUrl = getEnvOrNull("SPRING_DATASOURCE_URL");

        if (databaseUrl != null) {
            System.out.println(">>> Usando DATABASE_URL combinada (comprimento: " + databaseUrl.length() + ")");

            // Se já for JDBC, usar diretamente
            if (databaseUrl.startsWith("jdbc:")) {
                System.out.println(">>> Formato JDBC direto");
                return DataSourceBuilder.create()
                        .url(databaseUrl)
                        .driverClassName("org.postgresql.Driver")
                        .build();
            }

            // Parsing manual: postgres://USER:PASSWORD@HOST:PORT/DATABASE
            try {
                String url = databaseUrl;

                if (url.startsWith("postgres://")) {
                    url = url.substring("postgres://".length());
                } else if (url.startsWith("postgresql://")) {
                    url = url.substring("postgresql://".length());
                } else {
                    throw new RuntimeException("Formato de DATABASE_URL não reconhecido: " + url.substring(0, Math.min(15, url.length())));
                }

                int lastAtSign = url.lastIndexOf('@');
                if (lastAtSign < 0) {
                    throw new RuntimeException("DATABASE_URL sem '@' para separar credenciais do host");
                }

                String credentials = url.substring(0, lastAtSign);
                String hostPortDb = url.substring(lastAtSign + 1);

                int colonIndex = credentials.indexOf(':');
                String username = colonIndex > 0 ? credentials.substring(0, colonIndex) : credentials;
                String password = colonIndex > 0 ? credentials.substring(colonIndex + 1) : "";

                // Extrair query params
                String queryParams = null;
                int questionMark = hostPortDb.indexOf('?');
                if (questionMark > 0) {
                    queryParams = hostPortDb.substring(questionMark + 1);
                    hostPortDb = hostPortDb.substring(0, questionMark);
                }

                // Construir JDBC URL
                StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://");
                jdbcUrl.append(hostPortDb);

                if (queryParams != null && !queryParams.isEmpty()) {
                    jdbcUrl.append("?").append(queryParams);
                    if (!queryParams.contains("sslmode")) {
                        jdbcUrl.append("&sslmode=require");
                    }
                } else {
                    jdbcUrl.append("?sslmode=require");
                }

                System.out.println(">>> JDBC URL: " + jdbcUrl);
                System.out.println(">>> Username: " + username);

                return DataSourceBuilder.create()
                        .url(jdbcUrl.toString())
                        .username(username)
                        .password(password)
                        .driverClassName("org.postgresql.Driver")
                        .build();

            } catch (Exception e) {
                System.err.println(">>> ERRO ao parsear DATABASE_URL: " + e.getMessage());
                throw new RuntimeException("Falha ao configurar DataSource", e);
            }
        }

        // ============================================================
        // FALLBACK: Desenvolvimento local
        // ============================================================
        System.err.println("==========================================");
        System.err.println(">>> NENHUMA variável de banco encontrada!");
        System.err.println(">>> Checadas: DB_URL, DATABASE_URL, DATABASE_INTERNAL_URL");
        System.err.println(">>> Usando fallback localhost:5432");
        System.err.println("==========================================");

        return DataSourceBuilder.create()
                .url("jdbc:postgresql://localhost:5432/inkflow")
                .username("postgres")
                .password("postgres")
                .driverClassName("org.postgresql.Driver")
                .build();
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
