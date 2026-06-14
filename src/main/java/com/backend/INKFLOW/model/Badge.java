package com.backend.INKFLOW.model;

import jakarta.persistence.*;

@Entity
@Table(name = "badges")
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false)
    private String icone;

    @Column(nullable = false)
    private String categoria;

    @Column(name = "criterio_tipo", nullable = false)
    private String criterioTipo;

    @Column(name = "criterio_valor", nullable = false)
    private Integer criterioValor;

    public Badge() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getIcone() { return icone; }
    public void setIcone(String icone) { this.icone = icone; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getCriterioTipo() { return criterioTipo; }
    public void setCriterioTipo(String criterioTipo) { this.criterioTipo = criterioTipo; }

    public Integer getCriterioValor() { return criterioValor; }
    public void setCriterioValor(Integer criterioValor) { this.criterioValor = criterioValor; }
}
