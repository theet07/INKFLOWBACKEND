package com.backend.INKFLOW.controller;

import com.backend.INKFLOW.model.Artista;
import com.backend.INKFLOW.service.ArtistaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/artistas")
public class ArtistaController {

    @Autowired
    private ArtistaService artistaService;

    @GetMapping
    public List<Artista> getAll() {
        return artistaService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Artista> getById(@PathVariable Integer id) {
        return artistaService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
