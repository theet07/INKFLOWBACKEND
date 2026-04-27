package com.backend.INKFLOW.dto;

import java.time.LocalDateTime;

/**
 * DTO seguro para atualização de agendamentos.
 * Permite apenas campos que artistas/admins podem modificar.
 * Previne sobrescrita de campos protegidos (cliente, artista, createdAt, etc).
 */
public class AgendamentoUpdateRequest {
    private LocalDateTime dataHora;
    private String servico;
    private String descricao;
    private String regiao;
    private Double largura;
    private Double altura;
    private String tags;
    private String imagemReferenciaUrl;
    private String imagemResultadoUrl;
    private Double valorPago;
    private Double valorPendente;

    // Getters e Setters
    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getServico() {
        return servico;
    }

    public void setServico(String servico) {
        this.servico = servico;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getRegiao() {
        return regiao;
    }

    public void setRegiao(String regiao) {
        this.regiao = regiao;
    }

    public Double getLargura() {
        return largura;
    }

    public void setLargura(Double largura) {
        this.largura = largura;
    }

    public Double getAltura() {
        return altura;
    }

    public void setAltura(Double altura) {
        this.altura = altura;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getImagemReferenciaUrl() {
        return imagemReferenciaUrl;
    }

    public void setImagemReferenciaUrl(String imagemReferenciaUrl) {
        this.imagemReferenciaUrl = imagemReferenciaUrl;
    }

    public String getImagemResultadoUrl() {
        return imagemResultadoUrl;
    }

    public void setImagemResultadoUrl(String imagemResultadoUrl) {
        this.imagemResultadoUrl = imagemResultadoUrl;
    }

    public Double getValorPago() {
        return valorPago;
    }

    public void setValorPago(Double valorPago) {
        this.valorPago = valorPago;
    }

    public Double getValorPendente() {
        return valorPendente;
    }

    public void setValorPendente(Double valorPendente) {
        this.valorPendente = valorPendente;
    }
}
