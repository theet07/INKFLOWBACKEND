package com.backend.INKFLOW.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "badge_usuario", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"badge_id", "cliente_id"})
})
public class BadgeUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "badge_id", nullable = false)
    private Badge badge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(nullable = false)
    private Boolean desbloqueado = false;

    @Column(name = "data_desbloqueio")
    private LocalDateTime dataDesbloqueio;

    @Column(nullable = false)
    private Integer progresso = 0;

    public BadgeUsuario() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Badge getBadge() { return badge; }
    public void setBadge(Badge badge) { this.badge = badge; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Boolean getDesbloqueado() { return desbloqueado; }
    public void setDesbloqueado(Boolean desbloqueado) { this.desbloqueado = desbloqueado; }

    public LocalDateTime getDataDesbloqueio() { return dataDesbloqueio; }
    public void setDataDesbloqueio(LocalDateTime dataDesbloqueio) { this.dataDesbloqueio = dataDesbloqueio; }

    public Integer getProgresso() { return progresso; }
    public void setProgresso(Integer progresso) { this.progresso = progresso; }
}
