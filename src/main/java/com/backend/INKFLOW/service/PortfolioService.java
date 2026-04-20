package com.backend.INKFLOW.service;

import com.backend.INKFLOW.model.Artista;
import com.backend.INKFLOW.model.PortfolioItem;
import com.backend.INKFLOW.repository.ArtistaRepository;
import com.backend.INKFLOW.repository.PortfolioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class PortfolioService {

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private ArtistaRepository artistaRepository;

    public List<PortfolioItem> getByArtista(Integer artistaId) {
        return portfolioRepository.findByArtistaIdOrderByIdDesc(artistaId);
    }

    public Optional<PortfolioItem> getById(Long id) {
        return portfolioRepository.findById(id);
    }

    public Optional<PortfolioItem> save(Integer artistaId, String imagemUrl, String categoria, String descricao) {
        return artistaRepository.findById(artistaId).map(artista -> {
            PortfolioItem item = new PortfolioItem();
            item.setArtista(artista);
            item.setImagemUrl(imagemUrl);
            item.setCategoria(categoria);
            item.setDescricao(descricao);
            return portfolioRepository.save(item);
        });
    }

    public boolean delete(Long itemId, Integer artistaId) {
        return portfolioRepository.findById(itemId).map(item -> {
            if (!item.getArtista().getId().equals(artistaId)) return false;
            portfolioRepository.delete(item);
            return true;
        }).orElse(false);
    }
}
