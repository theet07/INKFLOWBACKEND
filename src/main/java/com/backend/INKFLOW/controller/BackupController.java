package com.backend.INKFLOW.controller;

import com.backend.INKFLOW.service.BackupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Endpoint de backup manual — restrito a ROLE_ADMIN.
 * Protegido via SecurityConfig: /api/v1/admin/** exige ROLE_ADMIN.
 */
@RestController
@RequestMapping("/api/v1/admin/backup")
public class BackupController {

    @Autowired
    private BackupService backupService;

    /**
     * GET /api/v1/admin/backup/download
     * Gera um arquivo .sql com todos os dados atuais e dispara o download.
     */
    @GetMapping("/download")
    public ResponseEntity<byte[]> download() {
        String sql = backupService.gerarSql();
        String filename = "inkflow_backup_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                + ".sql";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/sql"))
                .body(sql.getBytes(StandardCharsets.UTF_8));
    }
}
