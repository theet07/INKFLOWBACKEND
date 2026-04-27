package com.backend.INKFLOW.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ContatoRequest {

    @NotBlank(message = "Nome é obrigatório.")
    @Size(max = 60, message = "Nome deve ter no máximo 60 caracteres.")
    private String nome;

    @NotBlank(message = "Email é obrigatório.")
    @Email(message = "Formato de email inválido.")
    @Size(max = 100, message = "Email deve ter no máximo 100 caracteres.")
    private String email;

    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres.")
    private String telefone;

    @NotBlank(message = "Mensagem é obrigatória.")
    @Size(max = 2000, message = "Mensagem deve ter no máximo 2000 caracteres.")
    private String mensagem;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
}
