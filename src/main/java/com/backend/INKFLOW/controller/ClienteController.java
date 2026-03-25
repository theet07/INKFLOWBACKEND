package com.backend.INKFLOW.controller;

import com.backend.INKFLOW.model.Cliente;
import com.backend.INKFLOW.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = {"https://inkflowfrontend.vercel.app", "http://localhost:5173"})
public class ClienteController {
    
    @Autowired
    private ClienteService clienteService;
    
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
    public ResponseEntity<Void> deleteCliente(@PathVariable Long id) {
        clienteService.deleteCliente(id);
        return ResponseEntity.ok().build();
    }
}