package com.backend.INKFLOW.controller;

import com.backend.INKFLOW.model.Admin;
import com.backend.INKFLOW.model.AgendamentoDashboard;
import com.backend.INKFLOW.model.ArtistaDTO;
import com.backend.INKFLOW.model.ClienteDTO;
import com.backend.INKFLOW.service.AdminService;
import com.backend.INKFLOW.service.AgendamentoService;
import com.backend.INKFLOW.service.ArtistaService;
import com.backend.INKFLOW.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    public ResponseEntity<?> createAdmin(@RequestBody Admin admin) {
        if (adminService.getByEmail(admin.getEmail()).isPresent())
            return ResponseEntity.status(409).body(Map.of("message", "Email ja cadastrado."));
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
}
