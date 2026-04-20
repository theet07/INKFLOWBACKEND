package com.backend.INKFLOW.model;

public class ArtistaDTO {
    private Integer id;
    private String nome;
    private String role;
    private String especialidades;
    private String bio;
    private String fotoUrl;
    private Boolean ativo;
    private String email;

    public static ArtistaDTO fromEntity(Artista artista) {
        ArtistaDTO dto = new ArtistaDTO();
        dto.id = artista.getId();
        dto.nome = artista.getNome();
        dto.role = artista.getRole();
        dto.especialidades = artista.getEspecialidades();
        dto.bio = artista.getBio();
        dto.fotoUrl = artista.getFotoUrl();
        dto.ativo = artista.getAtivo();
        dto.email = artista.getEmail();
        return dto;
    }

    public Integer getId() { return id; }
    public String getNome() { return nome; }
    public String getRole() { return role; }
    public String getEspecialidades() { return especialidades; }
    public String getBio() { return bio; }
    public String getFotoUrl() { return fotoUrl; }
    public Boolean getAtivo() { return ativo; }
    public String getEmail() { return email; }
}
