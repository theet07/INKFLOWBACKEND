package com.backend.INKFLOW.controller;

import com.backend.INKFLOW.model.Cliente;
import com.backend.INKFLOW.security.JwtUtil;
import com.backend.INKFLOW.service.ClienteService;
import com.backend.INKFLOW.service.EmailService;
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

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtUtil jwtUtil;

    private static final String GMAIL_REGEX = "^[^@]+@gmail\\.com$";

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
        if (cliente.getEmail() == null || !cliente.getEmail().matches(GMAIL_REGEX))
            return ResponseEntity.status(422).body(Map.of("message", "Clientes devem utilizar obrigatoriamente um e-mail @gmail.com"));
        if (clienteService.existsByUsername(cliente.getUsername()))
            return ResponseEntity.badRequest().body(Map.of("message", "Username já cadastrado."));
        if (clienteService.existsByEmail(cliente.getEmail()))
            return ResponseEntity.status(409).body(Map.of("message", "Email já cadastrado."));
        return ResponseEntity.ok(clienteService.saveCliente(cliente));
    }

    /**
     * POST /api/clientes/solicitar-codigo
     * Pre-cadastro: salva o cliente com conta_verificada=false,
     * gera OTP de 6 digitos e envia por e-mail.
     */
    @PostMapping("/solicitar-codigo")
    public ResponseEntity<?> solicitarCodigo(@RequestBody Cliente cliente) {
        if (cliente.getEmail() == null || !cliente.getEmail().matches(GMAIL_REGEX))
            return ResponseEntity.status(422).body(Map.of("message", "Clientes devem utilizar obrigatoriamente um e-mail @gmail.com"));
        if (clienteService.existsByEmail(cliente.getEmail()))
            return ResponseEntity.status(409).body(Map.of("message", "Email já cadastrado."));
        if (clienteService.existsByUsername(cliente.getUsername()))
            return ResponseEntity.badRequest().body(Map.of("message", "Username já cadastrado."));

        Cliente salvo = clienteService.saveCliente(cliente);
        String codigo = clienteService.gerarEsalvarCodigo(salvo);

        try {
            emailService.enviarCodigoVerificacao(salvo.getEmail(), codigo);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Conta criada mas falha ao enviar e-mail. Tente reenviar o codigo."));
        }

        return ResponseEntity.ok(Map.of(
                "message", "Codigo de verificacao enviado para " + salvo.getEmail(),
                "email", salvo.getEmail()
        ));
    }

    /**
     * POST /api/clientes/verificar-codigo
     * Confirma o OTP. Se correto, ativa a conta e retorna o token JWT.
     * Body: { "email": "...", "codigo": "123456" }
     */
    @PostMapping("/verificar-codigo")
    public ResponseEntity<?> verificarCodigo(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String codigo = body.get("codigo");

        if (email == null || codigo == null)
            return ResponseEntity.badRequest().body(Map.of("message", "email e codigo sao obrigatorios."));

        return clienteService.verificarCodigo(email, codigo)
                .map(cliente -> {
                    String token = jwtUtil.generateToken(cliente.getEmail(), "ROLE_CLIENTE");
                    return ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "Conta verificada com sucesso!",
                            "token", token,
                            "user", Map.of(
                                    "id", cliente.getId(),
                                    "email", cliente.getEmail(),
                                    "nome", cliente.getFullName() != null ? cliente.getFullName() : "",
                                    "role", "ROLE_CLIENTE"
                            )
                    ));
                })
                .orElse(ResponseEntity.status(422)
                        .body(Map.of("message", "Codigo invalido ou expirado.")));
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
