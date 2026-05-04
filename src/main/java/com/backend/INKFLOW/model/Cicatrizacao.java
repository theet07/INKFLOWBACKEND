package com.backend.INKFLOW.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "cicatrizacoes")
public class Cicatrizacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agendamento_id", nullable = false, unique = true)
    private Agendamento agendamento;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @Column(name = "periodo_total_dias", nullable = false)
    private Integer periodoTotalDias;

    @Column(nullable = false)
    private String status = "ATIVA"; // ATIVA, CONCLUIDA, ABANDONADA

    @Column(name = "xp_total", nullable = false)
    private Integer xpTotal = 0;

    @Column(name = "dia_atual", nullable = false)
    private Integer diaAtual = 1;

    @Column(name = "fase_atual", nullable = false)
    private String faseAtual = "FASE_1_PRIMEIRAS_24H";

    public Cicatrizacao() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Agendamento getAgendamento() { return agendamento; }
    public void setAgendamento(Agendamento agendamento) { this.agendamento = agendamento; }

    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }

    public LocalDate getDataFim() { return dataFim; }
    public void setDataFim(LocalDate dataFim) { this.dataFim = dataFim; }

    public Integer getPeriodoTotalDias() { return periodoTotalDias; }
    public void setPeriodoTotalDias(Integer periodoTotalDias) { this.periodoTotalDias = periodoTotalDias; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getXpTotal() { return xpTotal; }
    public void setXpTotal(Integer xpTotal) { this.xpTotal = xpTotal; }

    public Integer getDiaAtual() { return diaAtual; }
    public void setDiaAtual(Integer diaAtual) { this.diaAtual = diaAtual; }

    public String getFaseAtual() { return faseAtual; }
    public void setFaseAtual(String faseAtual) { this.faseAtual = faseAtual; }
}
