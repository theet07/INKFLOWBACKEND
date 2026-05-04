package com.backend.INKFLOW.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "checklist_itens")
public class ChecklistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checkpoint_dia_id", nullable = false)
    private CheckpointDia checkpointDia;

    @Column(nullable = false)
    private String periodo; // MANHA, TARDE, NOITE

    @Column(nullable = false)
    private Integer ordem;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false)
    private Boolean concluido = false;

    @Column(name = "data_marcacao")
    private LocalDateTime dataMarcacao;

    public ChecklistItem() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public CheckpointDia getCheckpointDia() { return checkpointDia; }
    public void setCheckpointDia(CheckpointDia checkpointDia) { this.checkpointDia = checkpointDia; }

    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }

    public Integer getOrdem() { return ordem; }
    public void setOrdem(Integer ordem) { this.ordem = ordem; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Boolean getConcluido() { return concluido; }
    public void setConcluido(Boolean concluido) { this.concluido = concluido; }

    public LocalDateTime getDataMarcacao() { return dataMarcacao; }
    public void setDataMarcacao(LocalDateTime dataMarcacao) { this.dataMarcacao = dataMarcacao; }
}
