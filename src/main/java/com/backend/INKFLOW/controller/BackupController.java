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
                    "proximoBackup", "Diariamente as 03:00 UTC",
                    "timestamp", agora.toString()
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

    /** GET /api/v1/admin/backup/testar-backup — dispara o envio por e-mail imediatamente. */
    @GetMapping({"/api/v1/admin/backup/testar-backup", "/api/admin/backup/testar-backup"})
    public ResponseEntity<?> testarBackup() {
        try {
            String sql = backupService.gerarSql();
            String filename = "inkflow_backup_"
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                    + ".sql";
            emailService.enviarBackupEmail(sql, filename);
            return ResponseEntity.ok(Map.of("message", "Backup enviado por e-mail com sucesso."));
        } catch (Exception e) {
            log.error("Erro ao disparar backup por email: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Erro ao enviar backup: " + e.getMessage()));
        }
    }
}
