package com.backend.INKFLOW.controller;

import com.backend.INKFLOW.model.Admin;
import com.backend.INKFLOW.model.Artista;
import com.backend.INKFLOW.model.Cliente;
import com.backend.INKFLOW.security.JwtUtil;
import com.backend.INKFLOW.service.AdminService;
import com.backend.INKFLOW.service.ArtistaService;
import com.backend.INKFLOW.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ArtistaService artistaService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        String email = loginData.get("email");
        String password = loginData.get("password");

        // Login admin
        Optional<Admin> admin = adminService.getByEmail(email);
        if (admin.isPresent() && passwordEncoder.matches(password, admin.get().getPassword())) {
            String token = jwtUtil.generateToken(email, "ROLE_ADMIN");
            return ResponseEntity.ok(Map.of(
                "success", true,
                "token", token,
                "user", Map.of(
                    "id", admin.get().getId(),
                    "email", admin.get().getEmail(),
                    "nome", admin.get().getNome(),
                    "isAdmin", true,
                    "role", "ROLE_ADMIN"
                )
            ));
        }

        // Login tatuador
        Optional<Artista> artista = artistaService.getByEmail(email);
        if (artista.isPresent() && passwordEncoder.matches(password, artista.get().getPassword())) {
            String token = jwtUtil.generateToken(email, "ROLE_ARTISTA");
            Map<String, Object> artistaUser = new HashMap<>();
            artistaUser.put("id", artista.get().getId());
            artistaUser.put("artistaId", artista.get().getId());
            artistaUser.put("email", artista.get().getEmail());
            artistaUser.put("nome", artista.get().getNome());
            artistaUser.put("fotoUrl", artista.get().getFotoUrl());
            artistaUser.put("bio", artista.get().getBio());
            artistaUser.put("especialidades", artista.get().getEspecialidades());
            artistaUser.put("isArtist", true);
            artistaUser.put("role", "ROLE_ARTISTA");
            return ResponseEntity.ok(Map.of("success", true, "token", token, "user", artistaUser));
        }

        // Login cliente
        Optional<Cliente> cliente = clienteService.getUserByEmail(email);
        if (cliente.isPresent() && passwordEncoder.matches(password, cliente.get().getPassword())) {
            if (!Boolean.TRUE.equals(cliente.get().getContaVerificada())) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "Conta nao verificada. Confirme seu e-mail antes de fazer login."
                ));
            }
            String token = jwtUtil.generateToken(email, "ROLE_CLIENTE");
            Map<String, Object> clienteUser = new HashMap<>();
            clienteUser.put("id", cliente.get().getId());
            clienteUser.put("email", cliente.get().getEmail());
            clienteUser.put("nome", cliente.get().getFullName());
            clienteUser.put("telefone", cliente.get().getTelefone());
            clienteUser.put("fotoUrl", cliente.get().getProfileImage());
            clienteUser.put("isAdmin", false);
            clienteUser.put("role", "ROLE_CLIENTE");
            return ResponseEntity.ok(Map.of("success", true, "token", token, "user", clienteUser));
        }

        return ResponseEntity.status(401).body(Map.of(
            "success", false,
            "message", "Credenciais invalidas."
        ));
    }
}
