package com.backend.INKFLOW.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");

        if (databaseUrl != null && !databaseUrl.isEmpty()) {
            try {
                // Render pode fornecer postgres:// ou postgresql://
                // Precisamos normalizar para o formato JDBC
                String cleanUrl = databaseUrl;

                // Se já for uma URL JDBC completa, usar diretamente
                if (cleanUrl.startsWith("jdbc:")) {
                    return DataSourceBuilder
                            .create()
                            .url(cleanUrl)
                            .driverClassName("org.postgresql.Driver")
                            .build();
                }

                // Normalizar postgres:// para postgresql:// para URI parsing
                if (cleanUrl.startsWith("postgres://")) {
                    cleanUrl = "postgresql://" + cleanUrl.substring("postgres://".length());
                }

                URI dbUri = new URI(cleanUrl);

                String userInfo = dbUri.getUserInfo();
                String username = null;
                String password = null;

                if (userInfo != null && userInfo.contains(":")) {
                    String[] parts = userInfo.split(":", 2);
                    username = parts[0];
                    password = parts[1];
                }

                // Construir URL JDBC
                int port = dbUri.getPort();
                String host = dbUri.getHost();
                String path = dbUri.getPath();
                String query = dbUri.getQuery();

                StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://");
                jdbcUrl.append(host);
                if (port > 0) {
                    jdbcUrl.append(":").append(port);
                }
                jdbcUrl.append(path);

                // Adicionar sslmode se não presente (Render exige SSL)
                if (query != null && !query.isEmpty()) {
                    jdbcUrl.append("?").append(query);
                    if (!query.contains("sslmode")) {
                        jdbcUrl.append("&sslmode=require");
                    }
                } else {
                    jdbcUrl.append("?sslmode=require");
                }

                DataSourceBuilder<?> builder = DataSourceBuilder.create()
                        .url(jdbcUrl.toString())
                        .driverClassName("org.postgresql.Driver");

                if (username != null) {
                    builder.username(username);
                }
                if (password != null) {
                    builder.password(password);
                }

                System.out.println(">>> DataSource configurado com sucesso para: " + host + path);

                return builder.build();

            } catch (URISyntaxException e) {
                throw new RuntimeException("Erro ao parsear DATABASE_URL: " + e.getMessage(), e);
            }
        }

        // Fallback para desenvolvimento local (sem DATABASE_URL definida)
        System.out.println(">>> DATABASE_URL não encontrada, usando fallback local");
        return DataSourceBuilder
                .create()
                .url("jdbc:postgresql://localhost:5432/inkflow")
                .username("postgres")
                .password("postgres")
                .driverClassName("org.postgresql.Driver")
                .build();
    }
}
