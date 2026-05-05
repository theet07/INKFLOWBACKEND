package com.backend.INKFLOW.model;

import jakarta.persistence.*;

@Entity
@Table(name = "notificacao_preferencias", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"cliente_id"})
})
public class NotificacaoPreferencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false, unique = true)
    private Cliente cliente;

    @Column(name = "horario_manha", nullable = false)
    private String horarioManha = "08:00";

    @Column(name = "horario_tarde", nullable = false)
    private String horarioTarde = "14:00";

    @Column(name = "horario_noite", nullable = false)
    private String horarioNoite = "21:00";

    @Column(name = "notificacoes_ativas", nullable = false)
    private Boolean notificacoesAtivas = true;

    public NotificacaoPreferencia() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public String getHorarioManha() { return horarioManha; }
    public void setHorarioManha(String horarioManha) { this.horarioManha = horarioManha; }

    public String getHorarioTarde() { return horarioTarde; }
    public void setHorarioTarde(String horarioTarde) { this.horarioTarde = horarioTarde; }

    public String getHorarioNoite() { return horarioNoite; }
    public void setHorarioNoite(String horarioNoite) { this.horarioNoite = horarioNoite; }

    public Boolean getNotificacoesAtivas() { return notificacoesAtivas; }
    public void setNotificacoesAtivas(Boolean notificacoesAtivas) { this.notificacoesAtivas = notificacoesAtivas; }
}
