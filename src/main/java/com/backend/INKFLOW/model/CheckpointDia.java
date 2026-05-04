package com.backend.INKFLOW.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "checkpoint_dias")
public class CheckpointDia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cicatrizacao_id", nullable = false)
    private Cicatrizacao cicatrizacao;

    @Column(name = "numero_dia", nullable = false)
    private Integer numeroDia;

    @Column(nullable = false)
    private String fase; // FASE_1_PRIMEIRAS_24H, FASE_2_INICIAL, FASE_3_DESCAMACAO, FASE_4_PROFUNDA

    @Column(name = "status_dia", nullable = false)
    private String statusDia = "BLOQUEADO"; // BLOQUEADO, DISPONIVEL, COMPLETO, PARCIAL, PERDIDO

    @Column(name = "xp_ganho", nullable = false)
    private Integer xpGanho = 0;

    @Column(nullable = false)
    private Integer estrelas = 0; // 0, 1, 2, 3

    @Column(name = "tem_quiz", nullable = false)
    private Boolean temQuiz = false;

    @Column(nullable = false)
    private LocalDate data;

    public CheckpointDia() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Cicatrizacao getCicatrizacao() { return cicatrizacao; }
    public void setCicatrizacao(Cicatrizacao cicatrizacao) { this.cicatrizacao = cicatrizacao; }

    public Integer getNumeroDia() { return numeroDia; }
    public void setNumeroDia(Integer numeroDia) { this.numeroDia = numeroDia; }

    public String getFase() { return fase; }
    public void setFase(String fase) { this.fase = fase; }

    public String getStatusDia() { return statusDia; }
    public void setStatusDia(String statusDia) { this.statusDia = statusDia; }

    public Integer getXpGanho() { return xpGanho; }
    public void setXpGanho(Integer xpGanho) { this.xpGanho = xpGanho; }

    public Integer getEstrelas() { return estrelas; }
    public void setEstrelas(Integer estrelas) { this.estrelas = estrelas; }

    public Boolean getTemQuiz() { return temQuiz; }
    public void setTemQuiz(Boolean temQuiz) { this.temQuiz = temQuiz; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
}
