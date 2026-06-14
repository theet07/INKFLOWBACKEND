package com.backend.INKFLOW.controller;

import com.backend.INKFLOW.service.FotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Autowired
    private FotoService fotoService;

    /**
     * Endpoint proxy para upload de imagens no Cloudinary.
     * Esconde upload_preset e credenciais do frontend.
     * Permite upload de CLIENTES (para referências de agendamento), ARTISTAS e ADMINS.
     */
    @PostMapping
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file,
                                         @RequestParam(value = "folder", required = false) String folder,
                                         Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Autenticacao necessaria para upload."));
        }

        // Valida se é CLIENTE, ARTISTA ou ADMIN
        boolean isAuthorized = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE") ||
                              a.getAuthority().equals("ROLE_ARTISTA") || 
                              a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAuthorized) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Acesso negado."));
        }

        try {
            // Usa o folder fornecido ou default "portfolio"
            String publicId = folder != null ? folder : "portfolio";
            String imageUrl = fotoService.upload(file, publicId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "url", imageUrl,
                "message", "Upload realizado com sucesso."
            ));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("message", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Erro ao fazer upload da imagem."));
        }
    }
}
