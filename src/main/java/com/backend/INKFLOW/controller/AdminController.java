package com.backend.INKFLOW.controller;

import com.backend.INKFLOW.dto.AdminCreateRequest;
import com.backend.INKFLOW.model.Admin;
import com.backend.INKFLOW.model.AgendamentoDashboard;
import com.backend.INKFLOW.model.ArtistaDTO;
import com.backend.INKFLOW.model.ClienteDTO;
import com.backend.INKFLOW.service.AdminService;
import com.backend.INKFLOW.service.AgendamentoService;
import com.backend.INKFLOW.service.ArtistaService;
import com.backend.INKFLOW.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired private AdminService adminService;
    @Autowired private AgendamentoService agendamentoService;
    @Autowired private ArtistaService artistaService;
    @Autowired private ClienteService clienteService;

    @PostMapping("/admins")
    public ResponseEntity<?> createAdmin(@Valid @RequestBody AdminCreateRequest request) {
        if (adminService.getByEmail(request.getEmail()).isPresent())
            return ResponseEntity.status(409).body(Map.of("message", "Email ja cadastrado."));
        
        Admin admin = new Admin();
        admin.setNome(request.getNome());
        admin.setEmail(request.getEmail());
        admin.setPassword(request.getPassword());
        
        return ResponseEntity.ok(adminService.save(admin));
    }

    @GetMapping("/agendamentos")
    public List<AgendamentoDashboard> getAllAgendamentos() {
        return agendamentoService.getAllAgendamentos()
                .stream().map(AgendamentoDashboard::new).toList();
    }

    @GetMapping("/appointments")
    public List<AgendamentoDashboard> getAllAppointments() {
        return getAllAgendamentos();
    }

    @GetMapping("/artistas")
    public List<ArtistaDTO> getAllArtistas() {
        return artistaService.getAll()
                .stream().map(ArtistaDTO::fromEntity).toList();
    }

    @GetMapping("/clientes")
    public List<ClienteDTO> getAllClientes() {
        return clienteService.getAllClientes()
                .stream().map(ClienteDTO::fromEntity).toList();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(Map.of("message", errors.values().iterator().next()));
    }
}
