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
        // Tentar múltiplas variáveis de ambiente que o Render pode usar
        String databaseUrl = System.getenv("DATABASE_URL");

        if (databaseUrl == null || databaseUrl.isEmpty()) {
            databaseUrl = System.getenv("DATABASE_INTERNAL_URL");
        }
        if (databaseUrl == null || databaseUrl.isEmpty()) {
            databaseUrl = System.getenv("DATABASE_EXTERNAL_URL");
        }

        // Também tentar variáveis Spring padrão
        if (databaseUrl == null || databaseUrl.isEmpty()) {
            databaseUrl = System.getenv("SPRING_DATASOURCE_URL");
        }

        if (databaseUrl != null && !databaseUrl.isEmpty()) {
            System.out.println(">>> DATABASE_URL encontrada (comprimento: " + databaseUrl.length() + ")");
            try {
                // Se já for uma URL JDBC completa, usar diretamente
                if (databaseUrl.startsWith("jdbc:")) {
                    System.out.println(">>> Usando URL JDBC direta");

                    DataSourceBuilder<?> builder = DataSourceBuilder.create()
                            .url(databaseUrl)
                            .driverClassName("org.postgresql.Driver");

                    // Verificar se username/password estão em variáveis separadas
                    String username = System.getenv("DATABASE_USERNAME");
                    String password = System.getenv("DATABASE_PASSWORD");
                    if (username == null) username = System.getenv("SPRING_DATASOURCE_USERNAME");
                    if (password == null) password = System.getenv("SPRING_DATASOURCE_PASSWORD");
                    if (username != null) builder.username(username);
                    if (password != null) builder.password(password);

                    return builder.build();
                }

                // Normalizar postgres:// para postgresql:// para URI parsing
                String cleanUrl = databaseUrl;
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
                if (path != null) {
                    jdbcUrl.append(path);
                }

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

                if (username != null) builder.username(username);
                if (password != null) builder.password(password);

                System.out.println(">>> DataSource configurado com sucesso para: " + host + path);

                return builder.build();

            } catch (URISyntaxException e) {
                System.err.println(">>> ERRO ao parsear DATABASE_URL: " + e.getMessage());
                throw new RuntimeException("Erro ao parsear DATABASE_URL", e);
            }
        }

        // Se nenhuma variável foi encontrada, logar as variáveis disponíveis para debug
        System.err.println("==========================================");
        System.err.println(">>> ERRO CRITICO: Nenhuma DATABASE_URL encontrada!");
        System.err.println(">>> Variáveis de ambiente verificadas:");
        System.err.println(">>>   DATABASE_URL = " + (System.getenv("DATABASE_URL") != null ? "DEFINIDA" : "NULL"));
        System.err.println(">>>   DATABASE_INTERNAL_URL = " + (System.getenv("DATABASE_INTERNAL_URL") != null ? "DEFINIDA" : "NULL"));
        System.err.println(">>>   DATABASE_EXTERNAL_URL = " + (System.getenv("DATABASE_EXTERNAL_URL") != null ? "DEFINIDA" : "NULL"));
        System.err.println(">>>   SPRING_DATASOURCE_URL = " + (System.getenv("SPRING_DATASOURCE_URL") != null ? "DEFINIDA" : "NULL"));
        System.err.println(">>> Usando fallback localhost:5432 (vai falhar no Render!)");
        System.err.println("==========================================");

        return DataSourceBuilder
                .create()
                .url("jdbc:postgresql://localhost:5432/inkflow")
                .username("postgres")
                .password("postgres")
                .driverClassName("org.postgresql.Driver")
                .build();
    }
}
