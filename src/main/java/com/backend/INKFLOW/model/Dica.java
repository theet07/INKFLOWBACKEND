package com.backend.INKFLOW.model;

import jakarta.persistence.*;

@Entity
@Table(name = "dicas")
public class Dica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, length = 500)
    private String descricao;

    @Column(nullable = false)
    private String icone;

    @Column(name = "dia_inicio", nullable = false)
    private Integer diaInicio;

    @Column(name = "dia_fim", nullable = false)
    private Integer diaFim;

    public Dica() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getIcone() { return icone; }
    public void setIcone(String icone) { this.icone = icone; }

    public Integer getDiaInicio() { return diaInicio; }
    public void setDiaInicio(Integer diaInicio) { this.diaInicio = diaInicio; }

    public Integer getDiaFim() { return diaFim; }
    public void setDiaFim(Integer diaFim) { this.diaFim = diaFim; }
}
