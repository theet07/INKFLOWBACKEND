package com.backend.INKFLOW.controller;

import com.backend.INKFLOW.model.ArtistaDTO;
import com.backend.INKFLOW.model.ArtistaVitrine;
import com.backend.INKFLOW.service.ArtistaService;
import com.backend.INKFLOW.service.DisponibilidadeService;
import com.backend.INKFLOW.service.FotoService;
import com.backend.INKFLOW.service.PortfolioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/api/artistas", "/api/artists"})
public class ArtistaController {

    @Autowired private ArtistaService artistaService;
    @Autowired private PortfolioService portfolioService;
    @Autowired private DisponibilidadeService disponibilidadeService;
    @Autowired private FotoService fotoService;

    @GetMapping
    public List<ArtistaVitrine> getAll() {
        return artistaService.getAll().stream()
                .map(ArtistaVitrine::new)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArtistaVitrine> getById(@PathVariable Integer id) {
        return artistaService.getById(id)
                .map(artista -> new ArtistaVitrine(artista, portfolioService.getByArtista(id)))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<?> getAvailability(@PathVariable Integer id,
                                              @RequestParam(required = false) Integer ano,
                                              @RequestParam(required = false) Integer mes) {
        if (artistaService.getById(id).isEmpty())
            return ResponseEntity.notFound().build();

        int anoParam = ano != null ? ano : LocalDate.now().getYear();
        int mesParam = mes != null ? mes : LocalDate.now().getMonthValue();

        Map<String, List<String>> calendario =
                disponibilidadeService.getCalendarioMensal(id, anoParam, mesParam);

        List<Map<String, Object>> dias = calendario.entrySet().stream()
                .map(e -> {
                    Map<String, Object> dia = new HashMap<>();
                    dia.put("data", e.getKey());
                    dia.put("disponivel", !e.getValue().isEmpty());
                    dia.put("slots", e.getValue());
                    return dia;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(dias);
    }

    /** PUT /api/artistas/{id} — atualiza nome, bio e especialidades */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateArtista(@PathVariable Integer id,
                                            @RequestBody Map<String, String> body,
                                            Authentication auth) {
        return artistaService.getByEmail(auth.getName())
                .filter(a -> a.getId().equals(id))
                .map(artista -> {
                    if (body.containsKey("nome")) artista.setNome(body.get("nome"));
                    if (body.containsKey("bio")) artista.setBio(body.get("bio"));
                    if (body.containsKey("especialidades")) artista.setEspecialidades(body.get("especialidades"));
                    return ResponseEntity.ok(ArtistaDTO.fromEntity(artistaService.save(artista)));
                })
                .orElse(ResponseEntity.status(403).build());
    }

    /** POST /api/artistas/{id}/foto — upload de foto de perfil do artista */
    @PostMapping("/{id}/foto")
    public ResponseEntity<?> uploadFoto(@PathVariable Integer id,
                                         @RequestParam("file") MultipartFile file,
                                         Authentication auth) {
        return artistaService.getByEmail(auth.getName())
                .filter(a -> a.getId().equals(id))
                .map(artista -> {
                    try {
                        if (artista.getFotoUrl() != null) {
                            String oldPublicId = fotoService.extractPublicId(artista.getFotoUrl());
                            if (oldPublicId != null) fotoService.delete(oldPublicId);
                        }
                        String url = fotoService.upload(file, "artista_" + id);
                        artista.setFotoUrl(url);
                        artistaService.save(artista);
                        return ResponseEntity.ok(Map.of("fotoUrl", url));
                    } catch (Exception e) {
                        return ResponseEntity.internalServerError()
                                .body(Map.of("message", "Erro ao fazer upload da foto."));
                    }
                })
                .orElse(ResponseEntity.status(403).build());
    }
}
