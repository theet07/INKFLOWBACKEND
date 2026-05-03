package com.backend.INKFLOW.service;

import com.backend.INKFLOW.dto.LeadArtistaRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailAsyncService {
    private static final Logger log = LoggerFactory.getLogger(EmailAsyncService.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String remetente;
    
    public void enviarEmailsLead(LeadArtistaRequest request, String emailTrimmed, String whatsappLimpo) {
        try {
            log.info("[EMAIL] Enviando confirmação para: {}", emailTrimmed);
            SimpleMailMessage mailArtista = new SimpleMailMessage();
            mailArtista.setFrom(remetente);
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
            log.info("[EMAIL] Confirmação enviada com sucesso para: {}", emailTrimmed);
        } catch (Exception e) {
            log.error("[EMAIL] Erro ao enviar confirmação para {}: {}", emailTrimmed, e.getMessage(), e);
        }
    }
    
    public void enviarEmailTeste(String emailDestino) {
        try {
            log.info("[EMAIL] Enviando teste para: {}", emailDestino);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(remetente);
            message.setTo(emailDestino);
            message.setSubject("Teste de Email - InkFlow");
            message.setText("Este é um email de teste do sistema InkFlow.\n\nSe você recebeu esta mensagem, o sistema de email está funcionando corretamente!");
            mailSender.send(message);
            log.info("[EMAIL] Teste enviado com sucesso para: {}", emailDestino);
        } catch (Exception e) {
            log.error("[EMAIL] Erro ao enviar teste: {}", e.getMessage(), e);
        }
    }
}
