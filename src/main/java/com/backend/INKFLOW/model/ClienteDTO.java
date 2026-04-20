package com.backend.INKFLOW.model;

import java.time.LocalDateTime;

public class ClienteDTO {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String telefone;
    private String profileImage;
    private Boolean contaVerificada;
    private LocalDateTime createdAt;

    public static ClienteDTO fromEntity(Cliente cliente) {
        ClienteDTO dto = new ClienteDTO();
        dto.id = cliente.getId();
        dto.username = cliente.getUsername();
        dto.email = cliente.getEmail();
        dto.fullName = cliente.getFullName();
        dto.telefone = cliente.getTelefone();
        dto.profileImage = cliente.getProfileImage();
        dto.contaVerificada = cliente.getContaVerificada();
        dto.createdAt = cliente.getCreatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getTelefone() { return telefone; }
    public String getProfileImage() { return profileImage; }
    public Boolean getContaVerificada() { return contaVerificada; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
