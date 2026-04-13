package com.backend.INKFLOW.controller;

import com.backend.INKFLOW.model.Artista;
import com.backend.INKFLOW.model.Cliente;
import com.backend.INKFLOW.security.JwtUtil;
import com.backend.INKFLOW.service.ArtistaService;
import com.backend.INKFLOW.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ArtistaService artistaService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${ADMIN_EMAIL:admin@inkflow.com}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD_HASH:}")
    private String adminPasswordHash;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        String email = loginData.get("email");
        String password = loginData.get("password");

        // Login admin
        if (adminEmail.equals(email) && !adminPasswordHash.isBlank()
                && passwordEncoder.matches(password, adminPasswordHash)) {
            String token = jwtUtil.generateToken(email, "ROLE_ADMIN");
            return ResponseEntity.ok(Map.of(
                "success", true,
                "token", token,
                "user", Map.of("id", 0, "email", email, "nome", "Administrador", "isAdmin", true, "role", "ROLE_ADMIN")
            ));
        }

        // Login tatuador
        Optional<Artista> artista = artistaService.getByEmail(email);
        if (artista.isPresent() && passwordEncoder.matches(password, artista.get().getPassword())) {
            String token = jwtUtil.generateToken(email, "ROLE_ARTISTA");
            return ResponseEntity.ok(Map.of(
                "success", true,
                "token", token,
                "user", Map.of(
                    "id", artista.get().getId(),
                    "email", artista.get().getEmail(),
                    "nome", artista.get().getNome(),
                    "isArtist", true,
                    "role", "ROLE_ARTISTA"
                )
            ));
        }

        // Login cliente
        Optional<Cliente> cliente = clienteService.getUserByEmail(email);
        if (cliente.isPresent() && passwordEncoder.matches(password, cliente.get().getPassword())) {
            String token = jwtUtil.generateToken(email, "ROLE_CLIENTE");
            return ResponseEntity.ok(Map.of(
                "success", true,
                "token", token,
                "user", Map.of(
                    "id", cliente.get().getId(),
                    "email", cliente.get().getEmail(),
                    "nome", cliente.get().getFullName(),
                    "isAdmin", false,
                    "role", "ROLE_CLIENTE"
                )
            ));
        }

        return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "message", "Email ou senha incorretos"
        ));
    }
}
