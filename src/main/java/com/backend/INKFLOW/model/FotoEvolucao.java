package com.backend.INKFLOW.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fotos_evolucao")
public class FotoEvolucao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cicatrizacao_id", nullable = false)
    private Cicatrizacao cicatrizacao;

    @Column(name = "url_imagem", nullable = false)
    private String urlImagem;

    @Column(name = "numero_dia", nullable = false)
    private Integer numeroDia;

    @Column(name = "data_upload", nullable = false)
    private LocalDateTime dataUpload = LocalDateTime.now();

    private String legenda;

    public FotoEvolucao() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Cicatrizacao getCicatrizacao() { return cicatrizacao; }
    public void setCicatrizacao(Cicatrizacao cicatrizacao) { this.cicatrizacao = cicatrizacao; }

    public String getUrlImagem() { return urlImagem; }
    public void setUrlImagem(String urlImagem) { this.urlImagem = urlImagem; }

    public Integer getNumeroDia() { return numeroDia; }
    public void setNumeroDia(Integer numeroDia) { this.numeroDia = numeroDia; }

    public LocalDateTime getDataUpload() { return dataUpload; }
    public void setDataUpload(LocalDateTime dataUpload) { this.dataUpload = dataUpload; }

    public String getLegenda() { return legenda; }
    public void setLegenda(String legenda) { this.legenda = legenda; }
}
