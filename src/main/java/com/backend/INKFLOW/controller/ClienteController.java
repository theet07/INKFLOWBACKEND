package com.backend.INKFLOW.controller;

import com.backend.INKFLOW.model.Cliente;
import com.backend.INKFLOW.service.ClienteService;
import com.backend.INKFLOW.service.FotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private static final Logger log = LoggerFactory.getLogger(ClienteController.class);

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private FotoService fotoService;

    @GetMapping
    public List<Cliente> getAllClientes() {
        return clienteService.getAllClientes();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getClienteById(@PathVariable Long id, Authentication auth) {
        if (!isOwnerOrAdmin(id, auth))
            return ResponseEntity.status(403).body(Map.of("message", "Acesso negado."));
        return clienteService.getClienteById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Cliente> getClienteByEmail(@PathVariable String email) {
        return clienteService.getUserByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createCliente(@RequestBody Cliente cliente) {
        if (clienteService.existsByUsername(cliente.getUsername()))
            return ResponseEntity.badRequest().body(Map.of("message", "Username já cadastrado."));
        if (clienteService.existsByEmail(cliente.getEmail()))
            return ResponseEntity.status(409).body(Map.of("message", "Email já cadastrado."));
        return ResponseEntity.ok(clienteService.saveCliente(cliente));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCliente(@PathVariable Long id, @RequestBody Cliente cliente, Authentication auth) {
        if (!isOwnerOrAdmin(id, auth))
            return ResponseEntity.status(403).body(Map.of("message", "Acesso negado."));
        return clienteService.getClienteById(id)
                .map(existingCliente -> {
                    existingCliente.setFullName(cliente.getFullName());
                    existingCliente.setTelefone(cliente.getTelefone());
                    existingCliente.setProfileImage(cliente.getProfileImage());
                    return ResponseEntity.ok(clienteService.saveCliente(existingCliente));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCliente(@PathVariable Long id, Authentication auth) {
        if (!isOwnerOrAdmin(id, auth))
            return ResponseEntity.status(403).body(Map.of("message", "Acesso negado."));
        try {
            clienteService.deleteCliente(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Erro ao deletar cliente {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("message", "Erro ao deletar cliente: " + e.getMessage()));
        }
    }

    @PostMapping(value = "/{id}/foto", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadFoto(@PathVariable Long id, @RequestParam("file") MultipartFile file, Authentication auth) {
        if (!isOwnerOrAdmin(id, auth))
            return ResponseEntity.status(403).body(Map.of("message", "Acesso negado."));
        return clienteService.getClienteById(id).map(cliente -> {
            try {
                if (cliente.getProfileImage() != null) {
                    String oldPublicId = fotoService.extractPublicId(cliente.getProfileImage());
                    if (oldPublicId != null) fotoService.delete(oldPublicId);
                }
                String url = fotoService.upload(file, "cliente_" + id);
                cliente.setProfileImage(url);
                clienteService.saveCliente(cliente);
                return ResponseEntity.ok(Map.of("fotoUrl", url));
            } catch (Exception e) {
                log.error("Erro ao fazer upload da foto para cliente {}: {}", id, e.getMessage(), e);
                return ResponseEntity.internalServerError().body(Map.of("message", "Erro ao fazer upload da foto."));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/foto")
    public ResponseEntity<?> deleteFoto(@PathVariable Long id, Authentication auth) {
        if (!isOwnerOrAdmin(id, auth))
            return ResponseEntity.status(403).body(Map.of("message", "Acesso negado."));
        return clienteService.getClienteById(id).map(cliente -> {
            try {
                if (cliente.getProfileImage() != null) {
                    String oldPublicId = fotoService.extractPublicId(cliente.getProfileImage());
                    if (oldPublicId != null) fotoService.delete(oldPublicId);
                    cliente.setProfileImage(null);
                    clienteService.saveCliente(cliente);
                }
                return ResponseEntity.ok(Map.of("message", "Foto removida."));
            } catch (Exception e) {
                log.error("Erro ao remover foto do cliente {}: {}", id, e.getMessage(), e);
                return ResponseEntity.internalServerError().body(Map.of("message", "Erro ao remover foto."));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Verifica se o usuario autenticado e o dono do recurso ou ADMIN.
     * Extrai o email do JWT e compara com o email do cliente no banco.
     */
    private boolean isOwnerOrAdmin(Long clienteId, Authentication auth) {
        if (auth == null) return false;
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) return true;
        return clienteService.getClienteById(clienteId)
                .map(c -> c.getEmail().equals(auth.getName()))
                .orElse(false);
    }
}
