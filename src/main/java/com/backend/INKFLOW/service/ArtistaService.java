package com.backend.INKFLOW.service;

import com.backend.INKFLOW.model.Artista;
import com.backend.INKFLOW.repository.ArtistaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ArtistaService {

    @Autowired
    private ArtistaRepository artistaRepository;

    public List<Artista> getAll() {
        return artistaRepository.findByAtivoTrue();
    }

    public Optional<Artista> getById(Integer id) {
        return artistaRepository.findById(id);
    }
    
    public Optional<Artista> getById(Long id) {
        return artistaRepository.findById(id.intValue());
    }

    public Optional<Artista> getByEmail(String email) {
        return artistaRepository.findByEmail(email);
    }

    public Artista save(Artista artista) {
        return artistaRepository.save(artista);
    }
}
