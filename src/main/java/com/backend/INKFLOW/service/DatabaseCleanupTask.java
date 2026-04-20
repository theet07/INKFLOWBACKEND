package com.backend.INKFLOW.service;

import com.backend.INKFLOW.repository.ClienteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Tarefa agendada de limpeza do banco de dados.
 * Remove clientes que iniciaram o cadastro mas nao verificaram
 * o e-mail em 24 horas, evitando acumulo de registros inuteis.
 */
@Component
public class DatabaseCleanupTask {

    private static final Logger log = LoggerFactory.getLogger(DatabaseCleanupTask.class);

    @Autowired
    private ClienteRepository clienteRepository;

    /**
     * Roda todo dia as 03:05 UTC (00:05 Brasilia) — logo apos o backup.
     * Deleta clientes com conta_verificada = false criados ha mais de 24 horas.
     */
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
}
