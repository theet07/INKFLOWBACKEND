package com.backend.INKFLOW.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AgendamentoDashboard {

    private Long id;
    private LocalDateTime dataHora;
    private String status;
    private String servico;
    private String descricao;
    private String regiao;
    private Double largura;
    private Double altura;
    private List<String> tags;

    @JsonProperty("imagemReferenciaUrl")
    private String imagemReferenciaUrl;

    @JsonProperty("imagemResultadoUrl")
    private String imagemResultadoUrl;

    private Double preco;
    private Double valorPago;
    private Double valorPendente;
    private Integer avaliacao;
    private String observacoes;
    private boolean avaliado;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime updatedAt;

    private ClienteResumo cliente;
    private ArtistaResumo artista;

    public AgendamentoDashboard(Agendamento ag) {
        this.id = ag.getId();
        this.dataHora = ag.getDataHora();
        this.status = ag.getStatus();
        this.servico = ag.getServico();
        this.descricao = ag.getDescricao();
        this.regiao = ag.getRegiao();
        this.largura = ag.getLargura();
        this.altura = ag.getAltura();
        this.imagemReferenciaUrl = ag.getImagemReferenciaUrl();
        this.imagemResultadoUrl = ag.getImagemResultadoUrl();
        this.preco = ag.getPreco();
        this.valorPago = ag.getValorPago();
        this.valorPendente = ag.getValorPendente();
        this.avaliacao = ag.getAvaliacao();
        this.observacoes = ag.getObservacoes();
        this.avaliado = ag.isAvaliado();
        this.createdAt = ag.getCreatedAt();
        this.updatedAt = ag.getUpdatedAt();
        this.tags = ag.getTags() != null && !ag.getTags().isBlank()
                ? Arrays.stream(ag.getTags().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList()
                : Collections.emptyList();
        if (ag.getCliente() != null) this.cliente = new ClienteResumo(ag.getCliente());
        if (ag.getArtista() != null) this.artista = new ArtistaResumo(ag.getArtista());
    }

    // Getters
    public Long getId() { return id; }
    public LocalDateTime getDataHora() { return dataHora; }
    public String getStatus() { return status; }
    public String getServico() { return servico; }
    public String getDescricao() { return descricao; }
    public String getRegiao() { return regiao; }
    public Double getLargura() { return largura; }
    public Double getAltura() { return altura; }
    public List<String> getTags() { return tags; }
    public String getImagemReferenciaUrl() { return imagemReferenciaUrl; }
    public String getImagemResultadoUrl() { return imagemResultadoUrl; }
    public Double getPreco() { return preco; }
    public Double getValorPago() { return valorPago; }
    public Double getValorPendente() { return valorPendente; }
    public Integer getAvaliacao() { return avaliacao; }
    public String getObservacoes() { return observacoes; }
    public boolean isAvaliado() { return avaliado; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public ClienteResumo getCliente() { return cliente; }
    public ArtistaResumo getArtista() { return artista; }

    public static class ArtistaResumo {
        private Integer id;
        private String nome;
        private String fotoUrl;

        public ArtistaResumo(Artista a) {
            this.id = a.getId();
            this.nome = a.getNome();
            this.fotoUrl = a.getFotoUrl();
        }

        public Integer getId() { return id; }
        public String getNome() { return nome; }
        public String getFotoUrl() { return fotoUrl; }
    }

    // DTO interno do cliente — apenas dados de exibição, sem password
    public static class ClienteResumo {
        private Long id;
        private String nome;
        private String email;
        private String telefone;
        private String fotoUrl;

        public ClienteResumo(Cliente c) {
            this.id = c.getId();
            this.nome = c.getFullName();
            this.email = c.getEmail();
            this.telefone = c.getTelefone();
            this.fotoUrl = c.getProfileImage();
        }

        public Long getId() { return id; }
        public String getNome() { return nome; }
        public String getEmail() { return email; }
        public String getTelefone() { return telefone; }
        public String getFotoUrl() { return fotoUrl; }
    }
}
