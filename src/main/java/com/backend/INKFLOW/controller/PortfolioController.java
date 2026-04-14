package com.backend.INKFLOW.controller;

import com.backend.INKFLOW.model.PortfolioItem;
import com.backend.INKFLOW.service.ArtistaService;
import com.backend.INKFLOW.service.PortfolioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private ArtistaService artistaService;

    @GetMapping("/artista/{artistaId}")
    public List<PortfolioItem> getByArtista(@PathVariable Integer artistaId) {
        return portfolioService.getByArtista(artistaId);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body, Authentication auth) {
        Object artistaIdRaw = body.get("artistaId");
        if (!(artistaIdRaw instanceof Number)) {
            return ResponseEntity.badRequest().body(Map.of("message", "artistaId é obrigatório."));
        }
        Integer artistaId = ((Number) artistaIdRaw).intValue();

        // Ownership: artista só pode criar no próprio portfólio
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            boolean isOwner = artistaService.getByEmail(auth.getName())
                    .map(a -> a.getId().equals(artistaId))
                    .orElse(false);
            if (!isOwner) {
                return ResponseEntity.status(403)
                        .body(Map.of("message", "Você não tem permissão para adicionar itens neste portfólio."));
            }
        }

        String imagemUrl = (String) body.get("imagemUrl");
        String categoria = (String) body.get("categoria");
        String descricao = (String) body.get("descricao");

        if (imagemUrl == null || imagemUrl.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "imagemUrl é obrigatório."));
        }

        return portfolioService.save(artistaId, imagemUrl, categoria, descricao)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Integer artistaId = isAdmin ? null
                : artistaService.getByEmail(auth.getName())
                        .map(a -> a.getId())
                        .orElse(null);

        if (!isAdmin && artistaId == null) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Artista não encontrado para este token."));
        }

        // Admin pode deletar qualquer item — passa artistaId do próprio item
        if (isAdmin) {
            return portfolioService.getById(id).map(item -> {
                portfolioService.delete(id, item.getArtista().getId());
                return ResponseEntity.ok().<Void>build();
            }).orElse(ResponseEntity.notFound().build());
        }

        boolean deleted = portfolioService.delete(id, artistaId);
        if (!deleted) {
            return portfolioService.getById(id).isPresent()
                    ? ResponseEntity.status(403).body(Map.of("message", "Você não tem permissão para remover este item."))
                    : ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }
}
