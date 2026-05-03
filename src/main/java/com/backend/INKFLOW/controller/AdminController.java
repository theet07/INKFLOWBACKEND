package com.backend.INKFLOW.controller;

import com.backend.INKFLOW.dto.AdminCreateRequest;
import com.backend.INKFLOW.dto.AprovarArtistaRequest;
import com.backend.INKFLOW.model.*;
import com.backend.INKFLOW.repository.ArtistaRepository;
import com.backend.INKFLOW.repository.LeadArtistaRepository;
import com.backend.INKFLOW.service.AdminService;
import com.backend.INKFLOW.service.AgendamentoService;
import com.backend.INKFLOW.service.ArtistaService;
import com.backend.INKFLOW.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired private AdminService adminService;
    @Autowired private AgendamentoService agendamentoService;
    @Autowired private ArtistaService artistaService;
    @Autowired private ClienteService clienteService;
    @Autowired private LeadArtistaRepository leadArtistaRepository;
    @Autowired private ArtistaRepository artistaRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    // ── Stats ────────────────────────────────────────────────────
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        List<Agendamento> todos = agendamentoService.getAllAgendamentos();
        List<ClienteDTO> clientes = clienteService.getAllClientes()
                .stream().map(ClienteDTO::fromEntity).toList();
        List<ArtistaDTO> artistas = artistaService.getAll()
                .stream().map(ArtistaDTO::fromEntity).toList();

        long pendentes = todos.stream().filter(a ->
            "PENDENTE".equals(a.getStatus()) || "AGENDADO".equals(a.getStatus())).count();
        long confirmados = todos.stream().filter(a ->
            "CONFIRMADO".equals(a.getStatus()) || "EM_ANDAMENTO".equals(a.getStatus())).count();
        long concluidos = todos.stream().filter(a ->
            "REALIZADO".equals(a.getStatus()) || "FINALIZADO".equals(a.getStatus())).count();
        long cancelados = todos.stream().filter(a ->
            "CANCELADO".equals(a.getStatus())).count();

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long novosClientes = clientes.stream()
                .filter(c -> c.getCreatedAt() != null && c.getCreatedAt().isAfter(thirtyDaysAgo)).count();

        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        long agendamentosHoje = todos.stream()
                .filter(a -> a.getDataHora() != null
                        && !a.getDataHora().isBefore(startOfDay)
                        && a.getDataHora().isBefore(endOfDay)
                        && !"CANCELADO".equals(a.getStatus()))
                .count();

        double mediaAvaliacao = todos.stream()
                .filter(a -> a.getAvaliacao() != null && a.getAvaliacao() > 0)
                .mapToInt(Agendamento::getAvaliacao).average().orElse(0.0);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUsuarios", clientes.size() + artistas.size());
        stats.put("totalClientes", clientes.size());
        stats.put("totalArtistas", artistas.size());
        stats.put("totalAgendamentos", todos.size());
        stats.put("pendentes", pendentes);
        stats.put("confirmados", confirmados);
        stats.put("concluidos", concluidos);
        stats.put("cancelados", cancelados);
        stats.put("agendamentosHoje", agendamentosHoje);
        stats.put("novosClientes30d", novosClientes);
        stats.put("mediaAvaliacao", Math.round(mediaAvaliacao * 10.0) / 10.0);

        return ResponseEntity.ok(stats);
    }

    // ── Usuários unificados ──────────────────────────────────────
    @GetMapping("/usuarios")
    public ResponseEntity<?> getAllUsuarios() {
        List<Map<String, Object>> usuarios = new ArrayList<>();

        clienteService.getAllClientes().forEach(c -> {
            Map<String, Object> u = new LinkedHashMap<>();
            u.put("id", c.getId());
            u.put("nome", c.getFullName());
            u.put("email", c.getEmail());
            u.put("telefone", c.getTelefone());
            u.put("tipo", "CLIENTE");
            u.put("foto", c.getProfileImage());
            u.put("verificado", c.getContaVerificada());
            u.put("createdAt", c.getCreatedAt());
            usuarios.add(u);
        });

        artistaService.getAll().forEach(a -> {
            Map<String, Object> u = new LinkedHashMap<>();
            u.put("id", a.getId());
            u.put("nome", a.getNome());
            u.put("email", a.getEmail());
            u.put("telefone", null);
            u.put("tipo", "ARTISTA");
            u.put("foto", a.getFotoUrl());
            u.put("verificado", a.getAtivo());
            u.put("createdAt", null);
            usuarios.add(u);
        });

        return ResponseEntity.ok(usuarios);
    }

    // ── CRUD existente ───────────────────────────────────────────
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

    // ── Requisições de Artistas ──────────────────────────────────
    @GetMapping("/requisicoes-artista")
    public ResponseEntity<?> getAllRequisicoesArtista() {
        List<LeadArtista> requisicoes = leadArtistaRepository.findAll();
        return ResponseEntity.ok(requisicoes);
    }

    @GetMapping("/requisicoes-artista/pendentes/count")
    public ResponseEntity<?> getCountPendentes() {
        long count = leadArtistaRepository.findAll().stream()
                .filter(r -> "PENDENTE".equals(r.getStatus()))
                .count();
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PostMapping("/requisicoes-artista/{id}/aprovar")
    public ResponseEntity<?> aprovarRequisicao(
            @PathVariable Long id,
            @Valid @RequestBody AprovarArtistaRequest request) {
        
        // Buscar requisição
        Optional<LeadArtista> requisicaoOpt = leadArtistaRepository.findById(id);
        if (requisicaoOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "Requisição não encontrada."));
        }
        
        LeadArtista requisicao = requisicaoOpt.get();
        
        // Validar se já foi aprovada
        if ("APROVADO".equals(requisicao.getStatus())) {
            return ResponseEntity.status(400).body(Map.of("message", "Requisição já foi aprovada."));
        }
        
        // Validar se email já existe
        if (artistaRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(409).body(Map.of("message", "Email já cadastrado. Use outro email."));
        }
        
        try {
            // Criar artista
            Artista novoArtista = new Artista();
            novoArtista.setNome(requisicao.getNomeCompleto());
            novoArtista.setRole(requisicao.getNomeEstudio());
            novoArtista.setEspecialidades(requisicao.getEspecialidade());
            novoArtista.setEmail(request.getEmail());
            novoArtista.setPassword(passwordEncoder.encode(request.getSenha())); // Senha criptografada com BCrypt
            novoArtista.setAtivo(true);
            
            Artista artistaSalvo = artistaRepository.save(novoArtista);
            
            // Atualizar requisição
            requisicao.setStatus("APROVADO");
            requisicao.setAprovadoEm(LocalDateTime.now());
            // TODO: pegar ID do admin logado do token JWT
            // requisicao.setAprovadoPor(adminId);
            leadArtistaRepository.save(requisicao);
            
            return ResponseEntity.ok(Map.of(
                "message", "Artista aprovado e conta criada com sucesso.",
                "artistaId", artistaSalvo.getId()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Erro ao criar artista: " + e.getMessage()));
        }
    }

    @PostMapping("/requisicoes-artista/{id}/rejeitar")
    public ResponseEntity<?> rejeitarRequisicao(@PathVariable Long id) {
        
        // Buscar requisição
        Optional<LeadArtista> requisicaoOpt = leadArtistaRepository.findById(id);
        if (requisicaoOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "Requisição não encontrada."));
        }
        
        LeadArtista requisicao = requisicaoOpt.get();
        
        // Validar se já foi processada
        if ("APROVADO".equals(requisicao.getStatus())) {
            return ResponseEntity.status(400).body(Map.of("message", "Requisição já foi aprovada. Não pode ser rejeitada."));
        }
        
        if ("REJEITADO".equals(requisicao.getStatus())) {
            return ResponseEntity.status(400).body(Map.of("message", "Requisição já foi rejeitada."));
        }
        
        try {
            // Atualizar status para REJEITADO
            requisicao.setStatus("REJEITADO");
            leadArtistaRepository.save(requisicao);
            
            return ResponseEntity.ok(Map.of(
                "message", "Requisição rejeitada com sucesso."
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Erro ao rejeitar requisição: " + e.getMessage()));
        }
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

    // ── Atualizar Usuários ───────────────────────────────────────
    @PutMapping("/usuarios/cliente/{id}")
    public ResponseEntity<?> updateCliente(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        try {
            Optional<Cliente> clienteOpt = clienteService.getById(id);
            if (clienteOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("message", "Cliente não encontrado."));
            }
            
            Cliente cliente = clienteOpt.get();
            
            // Atualizar campos permitidos
            if (updates.containsKey("nome")) {
                cliente.setFullName((String) updates.get("nome"));
            }
            if (updates.containsKey("email")) {
                String newEmail = (String) updates.get("email");
                // Verificar se email já existe em outro cliente
                Optional<Cliente> existingCliente = clienteService.getByEmail(newEmail);
                if (existingCliente.isPresent() && !existingCliente.get().getId().equals(id)) {
                    return ResponseEntity.status(409).body(Map.of("message", "Email já cadastrado."));
                }
                cliente.setEmail(newEmail);
            }
            if (updates.containsKey("telefone")) {
                cliente.setTelefone((String) updates.get("telefone"));
            }
            if (updates.containsKey("verificado")) {
                cliente.setContaVerificada((Boolean) updates.get("verificado"));
            }
            
            clienteService.save(cliente);
            return ResponseEntity.ok(Map.of("message", "Cliente atualizado com sucesso."));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Erro ao atualizar cliente: " + e.getMessage()));
        }
    }

    @PutMapping("/usuarios/artista/{id}")
    public ResponseEntity<?> updateArtista(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        try {
            Optional<Artista> artistaOpt = artistaService.getById(id);
            if (artistaOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("message", "Artista não encontrado."));
            }
            
            Artista artista = artistaOpt.get();
            
            // Atualizar campos permitidos
            if (updates.containsKey("nome")) {
                artista.setNome((String) updates.get("nome"));
            }
            if (updates.containsKey("email")) {
                String newEmail = (String) updates.get("email");
                // Verificar se email já existe em outro artista
                Optional<Artista> existingArtista = artistaRepository.findByEmail(newEmail);
                if (existingArtista.isPresent() && !existingArtista.get().getId().equals(id)) {
                    return ResponseEntity.status(409).body(Map.of("message", "Email já cadastrado."));
                }
                artista.setEmail(newEmail);
            }
            if (updates.containsKey("verificado")) {
                artista.setAtivo((Boolean) updates.get("verificado"));
            }
            
            artistaRepository.save(artista);
            return ResponseEntity.ok(Map.of("message", "Artista atualizado com sucesso."));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Erro ao atualizar artista: " + e.getMessage()));
        }
    }
