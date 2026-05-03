package com.backend.INKFLOW.controller;

import com.backend.INKFLOW.dto.LeadArtistaRequest;
import com.backend.INKFLOW.model.LeadArtista;
import com.backend.INKFLOW.repository.LeadArtistaRepository;
import com.backend.INKFLOW.service.ChatRateLimitService;
import com.backend.INKFLOW.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/leads")
public class LeadController {

    private static final Logger log = LoggerFactory.getLogger(LeadController.class);

    @Autowired
    private LeadArtistaRepository leadRepository;

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private ChatRateLimitService rateLimitService;

    @PostMapping("/artista")
    public ResponseEntity<?> cadastrarLead(@RequestBody LeadArtistaRequest request,
                                           HttpServletRequest httpRequest) {
        // Rate limiting: 3 submissões por IP a cada 10 minutos
        String ip = getClientIp(httpRequest);
        if (!rateLimitService.isAllowed(ip)) {
            return ResponseEntity.status(429)
                    .body(Map.of("error", "Muitas solicitações. Aguarde alguns minutos."));
        }
        // Validações
        if (request.getNomeCompleto() == null || request.getNomeCompleto().trim().length() < 3) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nome completo deve ter no mínimo 3 caracteres."));
        }
        if (request.getNomeEstudio() == null || request.getNomeEstudio().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nome do estúdio é obrigatório."));
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "E-mail é obrigatório."));
        }
        String emailTrimmed = request.getEmail().trim();
        if (emailTrimmed.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "E-mail não pode ser vazio."));
        }
        if (!emailTrimmed.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return ResponseEntity.badRequest().body(Map.of("error", "E-mail inválido."));
        }
        if (request.getWhatsapp() == null || request.getWhatsapp().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "WhatsApp é obrigatório."));
        }
        if (request.getEspecialidade() == null || request.getEspecialidade().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Especialidade é obrigatória."));
        }

        // Limpar WhatsApp (remover formatação)
        String whatsappLimpo = request.getWhatsapp().replaceAll("[^0-9]", "");
        
        // Validar formato brasileiro (11 dígitos)
        if (whatsappLimpo.length() != 11) {
            return ResponseEntity.badRequest().body(Map.of("error", "WhatsApp deve ter 11 dígitos (DDD + número)."));
        }

        // Verificar duplicata
        if (leadRepository.findByWhatsapp(whatsappLimpo).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Este WhatsApp já está cadastrado."));
        }

        try {
            // Salvar no banco
            LeadArtista lead = new LeadArtista();
            lead.setNomeCompleto(request.getNomeCompleto().trim());
            lead.setNomeEstudio(request.getNomeEstudio().trim());
            lead.setEmail(emailTrimmed.toLowerCase());
            lead.setWhatsapp(whatsappLimpo);
            lead.setEspecialidade(request.getEspecialidade().trim());
            leadRepository.save(lead);

            // Enviar email de confirmação
            emailService.enviarEmailConfirmacaoLead(emailTrimmed, request.getNomeCompleto(), request.getNomeEstudio(), request.getEspecialidade(), request.getWhatsapp());

            return ResponseEntity.ok(Map.of(
                "message", "Cadastro realizado com sucesso! Entraremos em contato via WhatsApp.",
                "leadId", lead.getId()
            ));

        } catch (Exception e) {
            log.error("Erro ao processar cadastro de lead: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Erro ao processar cadastro."));
        }
    }
    
    @PostMapping("/test-email")
    public ResponseEntity<?> testEmail(@RequestBody Map<String, String> request) {
        String emailDestino = request.get("email");
        if (emailDestino == null || emailDestino.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email é obrigatório"));
        }
        
        try {
            emailService.enviarCodigoVerificacao(emailDestino, "123456");
            return ResponseEntity.ok(Map.of("message", "Email enviado", "destinatario", emailDestino));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}
