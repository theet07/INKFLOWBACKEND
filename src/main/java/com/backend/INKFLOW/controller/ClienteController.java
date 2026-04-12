package com.backend.INKFLOW.controller;

import com.backend.INKFLOW.model.Cliente;
import com.backend.INKFLOW.service.ClienteService;
import com.backend.INKFLOW.service.FotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = {"https://inkflowfrontend.vercel.app", "http://localhost:5173"})
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
    public ResponseEntity<Cliente> getClienteById(@PathVariable Long id) {
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
        if (clienteService.existsByUsername(cliente.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username já cadastrado."));
        }
        if (clienteService.existsByEmail(cliente.getEmail())) {
            return ResponseEntity.status(409).body(Map.of("message", "Email já cadastrado."));
        }
        return ResponseEntity.ok(clienteService.saveCliente(cliente));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Cliente> updateCliente(@PathVariable Long id, @RequestBody Cliente cliente) {
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
    public ResponseEntity<?> deleteCliente(@PathVariable Long id) {
        try {
            clienteService.deleteCliente(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Erro ao deletar cliente {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("message", "Erro ao deletar cliente: " + e.getMessage()));
        }
    }

    @PostMapping(value = "/{id}/foto", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadFoto(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
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
    public ResponseEntity<?> deleteFoto(@PathVariable Long id) {
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
}