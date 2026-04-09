package com.backend.INKFLOW.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() {
        // Tentar múltiplas variáveis de ambiente
        String databaseUrl = getEnvOrNull("DATABASE_URL");
        if (databaseUrl == null) databaseUrl = getEnvOrNull("DATABASE_INTERNAL_URL");
        if (databaseUrl == null) databaseUrl = getEnvOrNull("DATABASE_EXTERNAL_URL");
        if (databaseUrl == null) databaseUrl = getEnvOrNull("SPRING_DATASOURCE_URL");

        if (databaseUrl != null) {
            System.out.println(">>> DATABASE_URL encontrada (comprimento: " + databaseUrl.length() + ")");
            
            // Se já for JDBC, usar diretamente
            if (databaseUrl.startsWith("jdbc:")) {
                System.out.println(">>> Formato JDBC detectado, usando diretamente");
                return buildFromJdbcUrl(databaseUrl);
            }

            // Parsing manual para postgres:// ou postgresql://
            // Formato esperado: postgres://USER:PASSWORD@HOST:PORT/DATABASE
            try {
                String url = databaseUrl;

                // Remover prefixo do protocolo
                if (url.startsWith("postgres://")) {
                    url = url.substring("postgres://".length());
                } else if (url.startsWith("postgresql://")) {
                    url = url.substring("postgresql://".length());
                } else {
                    System.err.println(">>> Formato desconhecido: " + databaseUrl.substring(0, Math.min(20, databaseUrl.length())) + "...");
                    throw new RuntimeException("Formato de DATABASE_URL não reconhecido");
                }

                // Separar credenciais do host: USER:PASSWORD@HOST:PORT/DATABASE
                // O último @ separa credenciais do host (importante se a senha contém @)
                int lastAtSign = url.lastIndexOf('@');
                if (lastAtSign < 0) {
                    throw new RuntimeException("DATABASE_URL não contém '@' para separar credenciais do host");
                }

                String credentials = url.substring(0, lastAtSign);
                String hostPortDb = url.substring(lastAtSign + 1);

                // Extrair username e password (primeiro : separa)
                int colonIndex = credentials.indexOf(':');
                String username = colonIndex > 0 ? credentials.substring(0, colonIndex) : credentials;
                String password = colonIndex > 0 ? credentials.substring(colonIndex + 1) : "";

                // Extrair host, port e database de hostPortDb
                // Formato: HOST:PORT/DATABASE ou HOST:PORT/DATABASE?params
                String host;
                String port;
                String database;
                String queryParams = null;

                // Verificar se tem query params
                int questionMark = hostPortDb.indexOf('?');
                if (questionMark > 0) {
                    queryParams = hostPortDb.substring(questionMark + 1);
                    hostPortDb = hostPortDb.substring(0, questionMark);
                }

                // Separar host:port/database
                int slashIndex = hostPortDb.indexOf('/');
                String hostPort;
                if (slashIndex > 0) {
                    hostPort = hostPortDb.substring(0, slashIndex);
                    database = hostPortDb.substring(slashIndex + 1);
                } else {
                    hostPort = hostPortDb;
                    database = "";
                }

                int portColon = hostPort.indexOf(':');
                if (portColon > 0) {
                    host = hostPort.substring(0, portColon);
                    port = hostPort.substring(portColon + 1);
                } else {
                    host = hostPort;
                    port = "5432";
                }

                // Construir URL JDBC
                StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://");
                jdbcUrl.append(host).append(":").append(port);
                if (!database.isEmpty()) {
                    jdbcUrl.append("/").append(database);
                }

                // Adicionar SSL
                if (queryParams != null && !queryParams.isEmpty()) {
                    jdbcUrl.append("?").append(queryParams);
                    if (!queryParams.contains("sslmode")) {
                        jdbcUrl.append("&sslmode=require");
                    }
                } else {
                    jdbcUrl.append("?sslmode=require");
                }

                String finalUrl = jdbcUrl.toString();
                System.out.println(">>> Host: " + host);
                System.out.println(">>> Port: " + port);
                System.out.println(">>> DB: " + database);
                System.out.println(">>> User: " + username);
                System.out.println(">>> JDBC URL: " + finalUrl);

                return DataSourceBuilder.create()
                        .url(finalUrl)
                        .username(username)
                        .password(password)
                        .driverClassName("org.postgresql.Driver")
                        .build();

            } catch (Exception e) {
                System.err.println(">>> ERRO ao parsear DATABASE_URL: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Falha ao configurar DataSource a partir da DATABASE_URL", e);
            }
        }

        // Nenhuma variável encontrada
        System.err.println("==========================================");
        System.err.println(">>> ERRO CRITICO: Nenhuma DATABASE_URL encontrada!");
        System.err.println(">>>   DATABASE_URL = " + (System.getenv("DATABASE_URL") != null ? "DEFINIDA" : "NULL"));
        System.err.println(">>>   DATABASE_INTERNAL_URL = " + (System.getenv("DATABASE_INTERNAL_URL") != null ? "DEFINIDA" : "NULL"));
        System.err.println(">>>   DATABASE_EXTERNAL_URL = " + (System.getenv("DATABASE_EXTERNAL_URL") != null ? "DEFINIDA" : "NULL"));
        System.err.println(">>>   SPRING_DATASOURCE_URL = " + (System.getenv("SPRING_DATASOURCE_URL") != null ? "DEFINIDA" : "NULL"));
        System.err.println(">>> Usando fallback localhost (vai falhar no Render!)");
        System.err.println("==========================================");

        return DataSourceBuilder.create()
                .url("jdbc:postgresql://localhost:5432/inkflow")
                .username("postgres")
                .password("postgres")
                .driverClassName("org.postgresql.Driver")
                .build();
    }

    private DataSource buildFromJdbcUrl(String jdbcUrl) {
        DataSourceBuilder<?> builder = DataSourceBuilder.create()
                .url(jdbcUrl)
                .driverClassName("org.postgresql.Driver");

        String username = getEnvOrNull("DATABASE_USERNAME");
        if (username == null) username = getEnvOrNull("SPRING_DATASOURCE_USERNAME");
        String password = getEnvOrNull("DATABASE_PASSWORD");
        if (password == null) password = getEnvOrNull("SPRING_DATASOURCE_PASSWORD");

        if (username != null) builder.username(username);
        if (password != null) builder.password(password);

        return builder.build();
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
