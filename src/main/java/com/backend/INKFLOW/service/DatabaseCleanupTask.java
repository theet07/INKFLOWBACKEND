package com.backend.INKFLOW.service;

import com.backend.INKFLOW.repository.ClienteRepository;
import com.backend.INKFLOW.repository.TokenBlacklistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DatabaseCleanupTask {

    private static final Logger log = LoggerFactory.getLogger(DatabaseCleanupTask.class);

    @Autowired private ClienteRepository clienteRepository;
    @Autowired private TokenBlacklistRepository tokenBlacklistRepository;

    @Scheduled(cron = "0 5 3 * * *")
    public void limparClientesNaoVerificados() {
        LocalDateTime limite = LocalDateTime.now().minusHours(24);
        log.info("Iniciando limpeza de clientes nao verificados criados antes de {}", limite);
        try {
            int removidos = clienteRepository.deleteNaoVerificadosAntesDe(limite);
            if (removidos > 0) {
                log.info("Limpeza concluida: {} cliente(s) nao verificado(s) removido(s).", removidos);
            } else {
                log.info("Limpeza concluida: nenhum registro pendente encontrado.");
            }
        } catch (Exception e) {
            log.error("Falha na limpeza de clientes nao verificados: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 10 3 * * *")
    public void limparTokensExpirados() {
        log.info("Iniciando limpeza de tokens expirados da blacklist.");
        try {
            tokenBlacklistRepository.deleteByExpiresAtBefore(LocalDateTime.now());
            log.info("Limpeza de tokens expirados concluida.");
        } catch (Exception e) {
            log.error("Falha na limpeza de tokens expirados: {}", e.getMessage(), e);
        }
    }
}
