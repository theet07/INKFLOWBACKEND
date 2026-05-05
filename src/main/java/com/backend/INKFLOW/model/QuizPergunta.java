package com.backend.INKFLOW.model;

import jakarta.persistence.*;

@Entity
@Table(name = "quiz_perguntas")
public class QuizPergunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "checkpoint_dia_numero", nullable = false)
    private Integer checkpointDiaNumero;

    @Column(nullable = false, length = 500)
    private String pergunta;

    @Column(nullable = false)
    private String explicacao;

    @Column(name = "resposta_correta", nullable = false)
    private Integer respostaCorreta;

    @Column(name = "xp_bonus", nullable = false)
    private Integer xpBonus = 15;

    public QuizPergunta() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getCheckpointDiaNumero() { return checkpointDiaNumero; }
    public void setCheckpointDiaNumero(Integer checkpointDiaNumero) { this.checkpointDiaNumero = checkpointDiaNumero; }

    public String getPergunta() { return pergunta; }
    public void setPergunta(String pergunta) { this.pergunta = pergunta; }

    public String getExplicacao() { return explicacao; }
    public void setExplicacao(String explicacao) { this.explicacao = explicacao; }

    public Integer getRespostaCorreta() { return respostaCorreta; }
    public void setRespostaCorreta(Integer respostaCorreta) { this.respostaCorreta = respostaCorreta; }

    public Integer getXpBonus() { return xpBonus; }
    public void setXpBonus(Integer xpBonus) { this.xpBonus = xpBonus; }
}
