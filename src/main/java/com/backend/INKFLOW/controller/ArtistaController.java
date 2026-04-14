package com.backend.INKFLOW.controller;

import com.backend.INKFLOW.model.ArtistaVitrine;
import com.backend.INKFLOW.service.ArtistaService;
import com.backend.INKFLOW.service.PortfolioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/artistas")
public class ArtistaController {

    @Autowired
    private ArtistaService artistaService;

    @Autowired
    private PortfolioService portfolioService;

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
}
