package com.backend.INKFLOW.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAll(Exception e) {
        String mensagemSanitizada = sanitizar(e.getMessage());
        log.error("Erro nao tratado [{}]: {}", e.getClass().getSimpleName(), mensagemSanitizada);
        return ResponseEntity.internalServerError()
                .body(Map.of(
                        "error", e.getClass().getSimpleName(),
                        "message", mensagemSanitizada != null ? mensagemSanitizada : "erro interno"
                ));
    }

    /**
     * Remove da mensagem de erro qualquer dado sensivel:
     * hashes BCrypt ($2a$...) e valores de campos password/senha.
     */
    private String sanitizar(String msg) {
        if (msg == null) return null;
        return msg
                .replaceAll("\\$2[aby]\\$\\d{2}\\$[A-Za-z0-9./]{53}", "[HASH_REDACTED]")
                .replaceAll("(?i)(password|senha)\\s*[=:]\\s*\\S+", "$1=[REDACTED]");
    }
}
