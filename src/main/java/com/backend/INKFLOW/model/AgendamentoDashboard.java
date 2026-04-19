package com.backend.INKFLOW.model;

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

    private Double preco;
    private Double valorPago;
    private Double valorPendente;
    private Integer avaliacao;
    private String observacoes;
    private boolean avaliado;
    private ClienteResumo cliente;

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
        this.preco = ag.getPreco();
        this.valorPago = ag.getValorPago();
        this.valorPendente = ag.getValorPendente();
        this.avaliacao = ag.getAvaliacao();
        this.observacoes = ag.getObservacoes();
        this.avaliado = ag.isAvaliado();
        this.tags = ag.getTags() != null && !ag.getTags().isBlank()
                ? Arrays.stream(ag.getTags().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList()
                : Collections.emptyList();
        if (ag.getCliente() != null) {
            this.cliente = new ClienteResumo(ag.getCliente());
        }
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
    public Double getPreco() { return preco; }
    public Double getValorPago() { return valorPago; }
    public Double getValorPendente() { return valorPendente; }
    public Integer getAvaliacao() { return avaliacao; }
    public String getObservacoes() { return observacoes; }
    public boolean isAvaliado() { return avaliado; }
    public ClienteResumo getCliente() { return cliente; }

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
