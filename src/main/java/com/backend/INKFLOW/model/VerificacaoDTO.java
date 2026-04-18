package com.backend.INKFLOW.model;

public record VerificacaoDTO(String email, String codigo) {
    public String getEmail() { return email; }
    public String getCodigo() { return codigo; }
}
