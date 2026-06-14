package com.backend.INKFLOW.model;

import jakarta.persistence.*;

@Entity
@Table(name = "quiz_opcoes")
public class QuizOpcao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pergunta_id", nullable = false)
    private QuizPergunta pergunta;

    @Column(nullable = false)
    private Integer indice; // 0, 1, 2, 3

    @Column(nullable = false)
    private String texto;

    public QuizOpcao() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public QuizPergunta getPergunta() { return pergunta; }
    public void setPergunta(QuizPergunta pergunta) { this.pergunta = pergunta; }

    public Integer getIndice() { return indice; }
    public void setIndice(Integer indice) { this.indice = indice; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
}
