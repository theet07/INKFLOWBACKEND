package com.backend.INKFLOW.controller;

import com.backend.INKFLOW.model.Dica;
import com.backend.INKFLOW.repository.DicaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dicas")
public class DicaController {

    @Autowired
    private DicaRepository dicaRepository;

    @Cacheable(value = "dicas", key = "#numeroDia")
    @GetMapping("/dia/{numeroDia}")
    public ResponseEntity<List<Dica>> getDicasDia(@PathVariable Integer numeroDia) {
        List<Dica> dicas = dicaRepository.findByDia(numeroDia);
        return ResponseEntity.ok(dicas);
    }
}
