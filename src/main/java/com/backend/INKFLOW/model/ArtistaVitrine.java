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
    private List<PortfolioItemResumo> portfolio;

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
        this.portfolio = Collections.emptyList();
    }

    public ArtistaVitrine(Artista artista, List<PortfolioItem> items) {
        this(artista);
        this.portfolio = items.stream().map(PortfolioItemResumo::new).toList();
    }

    public Integer getId() { return id; }
    public String getNome() { return nome; }
    public String getFotoUrl() { return fotoUrl; }
    public String getRole() { return role; }
    public List<String> getEspecialidades() { return especialidades; }
    public String getBio() { return bio; }
    public List<PortfolioItemResumo> getPortfolio() { return portfolio; }

    public static class PortfolioItemResumo {
        private Long id;
        private String imagemUrl;
        private String categoria;
        private String descricao;

        public PortfolioItemResumo(PortfolioItem item) {
            this.id = item.getId();
            this.imagemUrl = item.getImagemUrl();
            this.categoria = item.getCategoria();
            this.descricao = item.getDescricao();
        }

        public Long getId() { return id; }
        public String getImagemUrl() { return imagemUrl; }
        public String getCategoria() { return categoria; }
        public String getDescricao() { return descricao; }
    }
}
