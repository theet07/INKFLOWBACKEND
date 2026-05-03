package com.backend.INKFLOW.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "leads_artistas")
public class LeadArtista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nomeCompleto;

    @Column(nullable = false)
    private String nomeEstudio;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String whatsapp;

    @Column(nullable = false)
    private String especialidade;

    @Column(name = "data_cadastro", nullable = false)
    private LocalDateTime dataCadastro;

    @Column(nullable = false)
    private String status = "PENDENTE"; // PENDENTE, APROVADO, REJEITADO

    @Column(name = "aprovado_por")
    private Long aprovadoPor;

    @Column(name = "aprovado_em")
    private LocalDateTime aprovadoEm;

    public LeadArtista() {
        this.dataCadastro = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public String getNomeEstudio() {
        return nomeEstudio;
    }

    public void setNomeEstudio(String nomeEstudio) {
        this.nomeEstudio = nomeEstudio;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(String whatsapp) {
        this.whatsapp = whatsapp;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    // Alias para compatibilidade com frontend
    public LocalDateTime getCreatedAt() {
        return dataCadastro;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getAprovadoPor() {
        return aprovadoPor;
    }

    public void setAprovadoPor(Long aprovadoPor) {
        this.aprovadoPor = aprovadoPor;
    }

    public LocalDateTime getAprovadoEm() {
        return aprovadoEm;
    }

    public void setAprovadoEm(LocalDateTime aprovadoEm) {
        this.aprovadoEm = aprovadoEm;
    }
}
