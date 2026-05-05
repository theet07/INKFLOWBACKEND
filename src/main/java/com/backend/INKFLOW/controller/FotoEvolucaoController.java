package com.backend.INKFLOW.controller;

import com.backend.INKFLOW.model.Cicatrizacao;
import com.backend.INKFLOW.model.FotoEvolucao;
import com.backend.INKFLOW.repository.CicatrizacaoRepository;
import com.backend.INKFLOW.repository.FotoEvolucaoRepository;
import com.backend.INKFLOW.service.FotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/fotos")
public class FotoEvolucaoController {

    @Autowired
    private FotoEvolucaoRepository fotoRepository;

    @Autowired
    private CicatrizacaoRepository cicatrizacaoRepository;

    @Autowired
    private FotoService fotoService;

    /** GET /api/fotos/cicatrizacao/{cicatrizacaoId} */
    @GetMapping("/cicatrizacao/{cicatrizacaoId}")
    public ResponseEntity<List<FotoEvolucao>> listar(@PathVariable Long cicatrizacaoId) {
        return ResponseEntity.ok(fotoRepository.findByCicatrizacaoIdOrderByNumeroDiaAsc(cicatrizacaoId));
    }

    /** POST /api/fotos/cicatrizacao/{cicatrizacaoId} */
    @PostMapping("/cicatrizacao/{cicatrizacaoId}")
    public ResponseEntity<?> upload(
            @PathVariable Long cicatrizacaoId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("numeroDia") Integer numeroDia,
            @RequestParam(value = "legenda", required = false) String legenda) {

        Optional<Cicatrizacao> cicOpt = cicatrizacaoRepository.findById(cicatrizacaoId);
        if (cicOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cicatrização não encontrada."));
        }

        try {
            String publicId = "inkflow/evolucao/cic_" + cicatrizacaoId + "_dia_" + numeroDia + "_" + System.currentTimeMillis();
            String url = fotoService.upload(file, publicId);

            FotoEvolucao foto = new FotoEvolucao();
            foto.setCicatrizacao(cicOpt.get());
            foto.setUrlImagem(url);
            foto.setNumeroDia(numeroDia);
            foto.setDataUpload(LocalDateTime.now());
            foto.setLegenda(legenda);

            fotoRepository.save(foto);
            return ResponseEntity.ok(foto);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Erro no upload: " + e.getMessage()));
        }
    }

    /** DELETE /api/fotos/{fotoId} */
    @DeleteMapping("/{fotoId}")
    public ResponseEntity<?> deletar(@PathVariable Long fotoId) {
        Optional<FotoEvolucao> fotoOpt = fotoRepository.findById(fotoId);
        if (fotoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            FotoEvolucao foto = fotoOpt.get();
            String publicId = fotoService.extractPublicId(foto.getUrlImagem());
            if (publicId != null) {
                fotoService.delete(publicId);
            }
            fotoRepository.delete(foto);
            return ResponseEntity.noContent().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Erro ao deletar: " + e.getMessage()));
        }
    }
}
