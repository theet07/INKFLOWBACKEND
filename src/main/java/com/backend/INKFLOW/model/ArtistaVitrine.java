package com.backend.INKFLOW.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ArtistaVitrine {

    private Integer id;
    private String nome;
    private String fotoUrl;
    private String role;
    private List<String> especialidades;
    private String bio;

    public ArtistaVitrine(Artista artista) {
        this.id = artista.getId();
        this.nome = artista.getNome();
        this.fotoUrl = artista.getFotoUrl();
        this.role = artista.getRole();
        this.bio = artista.getBio();
        this.especialidades = artista.getEspecialidades() != null && !artista.getEspecialidades().isBlank()
                ? Arrays.stream(artista.getEspecialidades().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList()
                : Collections.emptyList();
    }

    public Integer getId() { return id; }
    public String getNome() { return nome; }
    public String getFotoUrl() { return fotoUrl; }
    public String getRole() { return role; }
    public List<String> getEspecialidades() { return especialidades; }
    public String getBio() { return bio; }
}
