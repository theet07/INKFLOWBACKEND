package com.backend.INKFLOW.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "agendamentos")
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "artista_id")
    private Artista artista;

    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataHora;

    private String servico;
    private String descricao;
    private String status = "PENDENTE";
    private Double preco;
    private Integer avaliacao;
    private String observacoes;

    @Column(name = "valor_pago")
    private Double valorPago;

    @Column(name = "valor_pendente")
    private Double valorPendente;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "regiao", length = 100, nullable = true)
    private String regiao;

    @Column(name = "largura", nullable = true)
    private Double largura;

    @Column(name = "altura", nullable = true)
    private Double altura;

    @Column(name = "tags", length = 500, nullable = true)
    private String tags;

    @Column(name = "imagem_referencia_url", length = 1000, nullable = true)
    private String imagemReferenciaUrl;

    public Agendamento() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Artista getArtista() { return artista; }
    public void setArtista(Artista artista) { this.artista = artista; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public String getServico() { return servico; }
    public void setServico(String servico) { this.servico = servico; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getPreco() { return preco; }
    public void setPreco(Double preco) { this.preco = preco; }

    public Integer getAvaliacao() { return avaliacao; }
    public void setAvaliacao(Integer avaliacao) { this.avaliacao = avaliacao; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public Double getValorPago() { return valorPago; }
    public void setValorPago(Double valorPago) { this.valorPago = valorPago; }

    public Double getValorPendente() { return valorPendente; }
    public void setValorPendente(Double valorPendente) { this.valorPendente = valorPendente; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getRegiao() { return regiao; }
    public void setRegiao(String regiao) { this.regiao = regiao; }

    public Double getLargura() { return largura; }
    public void setLargura(Double largura) { this.largura = largura; }

    public Double getAltura() { return altura; }
    public void setAltura(Double altura) { this.altura = altura; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public String getImagemReferenciaUrl() { return imagemReferenciaUrl; }
    public void setImagemReferenciaUrl(String imagemReferenciaUrl) { this.imagemReferenciaUrl = imagemReferenciaUrl; }
}
