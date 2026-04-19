package com.backend.INKFLOW.controller;

import com.backend.INKFLOW.service.BackupService;
import com.backend.INKFLOW.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Endpoints de backup — restritos a ROLE_ADMIN.
 * Prefixo canonico: /api/v1/admin/backup
 * Alias legado:     /api/admin/backup  (redireciona para evitar 404/500)
 */
@RestController
public class BackupController {

    private static final Logger log = LoggerFactory.getLogger(BackupController.class);

    @Autowired
    private BackupService backupService;

    @Autowired
    private EmailService emailService;

    /** GET /api/v1/admin/backup/status — estado do servico de backup. */
    @GetMapping({"/api/v1/admin/backup/status", "/api/admin/backup/status"})
    public ResponseEntity<Map<String, Object>> status() {
        try {
            LocalDateTime agora = LocalDateTime.now();
            return ResponseEntity.ok(Map.of(
                    "status", "OK",
                    "servico", "BackupService",
                    "proximoBackup", "Diariamente as 00:00 (UTC)",
                    "timestamp", agora.toString(),
                    "webhookConfigurado", backupService.isWebhookConfigurado()
            ));
        } catch (Exception e) {
            log.error("Erro ao consultar status do backup: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of(
                    "status", "OK",
                    "servico", "BackupService",
                    "mensagem", "Nenhum backup executado ainda.",
                    "webhookConfigurado", false
            ));
        }
    }

    /** GET /api/v1/admin/backup/download — gera e baixa o .sql completo. */
    @GetMapping({"/api/v1/admin/backup/download", "/api/admin/backup/download"})
    public ResponseEntity<?> download() {
        try {
            String sql = backupService.gerarSql();
            String filename = "inkflow_backup_"
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                    + ".sql";
            log.info("Download de backup solicitado: {}", filename);
            emailService.enviarBackupEmail(sql, filename);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/sql"))
                    .body(sql.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Erro ao gerar backup para download: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Erro ao gerar backup: " + e.getMessage()));
        }
    }

    /** GET /api/v1/admin/backup/testar-webhook — dispara o envio para o Discord imediatamente. */
    @GetMapping({"/api/v1/admin/backup/testar-webhook", "/api/admin/backup/testar-webhook"})
    public ResponseEntity<?> testarWebhook() {
        if (!backupService.isWebhookConfigurado()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "BACKUP_WEBHOOK_URL nao configurada."));
        }
        try {
            String conteudo = backupService.gerarSql();
            backupService.enviarWebhook(conteudo);
            return ResponseEntity.ok(Map.of("message", "Webhook disparado. Verifique o canal do Discord."));
        } catch (Exception e) {
            log.error("Erro ao disparar webhook: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Erro ao disparar webhook: " + e.getMessage()));
        }
    }
}
