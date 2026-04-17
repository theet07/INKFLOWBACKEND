package com.backend.INKFLOW.model;

import jakarta.persistence.*;

/**
 * Define a disponibilidade semanal de um artista.
 * Cada registro representa um dia da semana (0=Seg, 6=Dom)
 * com o intervalo de horario de atendimento.
 */
@Entity
@Table(name = "disponibilidade_artistas",
       uniqueConstraints = @UniqueConstraint(columnNames = {"artista_id", "dia_semana"}))
public class DisponibilidadeArtista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artista_id", nullable = false)
    private Artista artista;

    /** 0 = Segunda, 1 = Terca, ..., 5 = Sabado, 6 = Domingo */
    @Column(name = "dia_semana", nullable = false)
    private Integer diaSemana;

    /** Hora de inicio no formato HH:mm, ex: "09:00" */
    @Column(name = "hora_inicio", nullable = false, length = 5)
    private String horaInicio;

    /** Hora de fim no formato HH:mm, ex: "18:00" */
    @Column(name = "hora_fim", nullable = false, length = 5)
    private String horaFim;

    /** Duracao padrao de cada slot em minutos (ex: 60) */
    @Column(name = "duracao_slot_minutos", nullable = false)
    private Integer duracaoSlotMinutos = 60;

    @Column(nullable = false)
    private Boolean ativo = true;

    public DisponibilidadeArtista() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Artista getArtista() { return artista; }
    public void setArtista(Artista artista) { this.artista = artista; }

    public Integer getDiaSemana() { return diaSemana; }
    public void setDiaSemana(Integer diaSemana) { this.diaSemana = diaSemana; }

    public String getHoraInicio() { return horaInicio; }
    public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }

    public String getHoraFim() { return horaFim; }
    public void setHoraFim(String horaFim) { this.horaFim = horaFim; }

    public Integer getDuracaoSlotMinutos() { return duracaoSlotMinutos; }
    public void setDuracaoSlotMinutos(Integer duracaoSlotMinutos) { this.duracaoSlotMinutos = duracaoSlotMinutos; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}
