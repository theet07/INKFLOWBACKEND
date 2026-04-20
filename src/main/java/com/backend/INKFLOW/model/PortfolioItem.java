package com.backend.INKFLOW.model;

import jakarta.persistence.*;

@Entity
@Table(name = "portfolio_items")
public class PortfolioItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "imagem_url", nullable = false, length = 1000)
    private String imagemUrl;

    private String categoria;

    @Column(length = 500)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artista_id", nullable = false)
    private Artista artista;

    public PortfolioItem() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getImagemUrl() { return imagemUrl; }
    public void setImagemUrl(String imagemUrl) { this.imagemUrl = imagemUrl; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Artista getArtista() { return artista; }
    public void setArtista(Artista artista) { this.artista = artista; }
}
