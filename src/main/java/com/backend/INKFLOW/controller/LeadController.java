package com.backend.INKFLOW.controller;

import com.backend.INKFLOW.dto.LeadArtistaRequest;
import com.backend.INKFLOW.model.LeadArtista;
import com.backend.INKFLOW.repository.LeadArtistaRepository;
import com.backend.INKFLOW.service.ChatRateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/leads")
public class LeadController {

    private static final Logger log = LoggerFactory.getLogger(LeadController.class);

    @Autowired
    private LeadArtistaRepository leadRepository;

    @Autowired
    private JavaMailSender mailSender;
    
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

            boolean emailArtistaEnviado = false;
            boolean emailEquipeEnviado = false;

            // Enviar email de confirmação para o artista
            try {
                log.info("Tentando enviar email para artista: {}", emailTrimmed);
                SimpleMailMessage mailArtista = new SimpleMailMessage();
                mailArtista.setFrom("inkflowstudios07@gmail.com");
                mailArtista.setTo(emailTrimmed);
                mailArtista.setSubject("Recebemos sua solicitação - InkFlow 🎨");
                mailArtista.setText(
                    "Olá, " + request.getNomeCompleto() + "!\n\n" +
                    "Recebemos sua solicitação para se tornar um artista parceiro do InkFlow.\n\n" +
                    "Dados recebidos:\n" +
                    "• Estúdio: " + request.getNomeEstudio() + "\n" +
                    "• Especialidade: " + request.getEspecialidade() + "\n" +
                    "• WhatsApp: " + request.getWhatsapp() + "\n\n" +
                    "Nossa equipe irá analisar sua solicitação e retornaremos em breve via WhatsApp ou email.\n\n" +
                    "Enquanto isso, siga-nos no Instagram @inkflowstudios para novidades!\n\n" +
                    "Atenciosamente,\n" +
                    "Equipe InkFlow"
                );
                mailSender.send(mailArtista);
                emailArtistaEnviado = true;
                log.info("Email enviado com sucesso para artista: {}", emailTrimmed);
            } catch (Exception e) {
                log.error("Erro ao enviar email para artista: {}", e.getMessage(), e);
            }

            // Enviar notificação para equipe InkFlow
            try {
                log.info("Tentando enviar notificação para equipe InkFlow");
                SimpleMailMessage mailEquipe = new SimpleMailMessage();
                mailEquipe.setTo("inkflowstudios07@gmail.com");
                mailEquipe.setSubject("🔥 Novo Lead de Artista - " + request.getNomeEstudio());
                mailEquipe.setText(
                    "NOVO LEAD CADASTRADO!\n\n" +
                    "Nome: " + request.getNomeCompleto() + "\n" +
                    "Estúdio: " + request.getNomeEstudio() + "\n" +
                    "E-mail: " + emailTrimmed + "\n" +
                    "WhatsApp: " + request.getWhatsapp() + "\n" +
                    "Especialidade: " + request.getEspecialidade() + "\n\n" +
                    "Link WhatsApp: https://wa.me/55" + whatsappLimpo + "\n\n" +
                    "Ação: Entrar em contato para liberar acesso beta."
                );
                mailSender.send(mailEquipe);
                emailEquipeEnviado = true;
                log.info("Notificação enviada com sucesso para equipe InkFlow");
            } catch (Exception e) {
                log.error("Erro ao enviar email para equipe: {}", e.getMessage(), e);
            }

            return ResponseEntity.ok(Map.of(
                "message", "Cadastro realizado com sucesso! Entraremos em contato via WhatsApp.",
                "leadId", lead.getId(),
                "emailArtistaEnviado", emailArtistaEnviado,
                "emailEquipeEnviado", emailEquipeEnviado
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
            log.info("Testando envio de email para: {}", emailDestino);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("inkflowstudios07@gmail.com");
            message.setTo(emailDestino);
            message.setSubject("Teste de Email - InkFlow");
            message.setText("Este é um email de teste do sistema InkFlow.\n\nSe você recebeu esta mensagem, o sistema de email está funcionando corretamente!");
            mailSender.send(message);
            log.info("Email de teste enviado com sucesso para: {}", emailDestino);
            return ResponseEntity.ok(Map.of("message", "Email enviado com sucesso!", "destinatario", emailDestino));
        } catch (Exception e) {
            log.error("Erro ao enviar email de teste: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Erro ao enviar email: " + e.getMessage()));
        }
    }
    
    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}
