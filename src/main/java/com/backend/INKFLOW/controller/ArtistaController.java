package com.backend.INKFLOW.controller;

import com.backend.INKFLOW.model.ArtistaVitrine;
import com.backend.INKFLOW.service.ArtistaService;
import com.backend.INKFLOW.service.DisponibilidadeService;
import com.backend.INKFLOW.service.PortfolioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/api/artistas", "/api/artists"})
public class ArtistaController {

    @Autowired
    private ArtistaService artistaService;

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private DisponibilidadeService disponibilidadeService;

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
}
