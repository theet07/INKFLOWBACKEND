package com.backend.INKFLOW.service;

import com.backend.INKFLOW.dto.LeadArtistaRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailAsyncService {
    private static final Logger log = LoggerFactory.getLogger(EmailAsyncService.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String remetente;
    
    @Async
    public void enviarEmailsLead(LeadArtistaRequest request, String emailTrimmed, String whatsappLimpo) {
        // Enviar email de confirmação para o artista
        try {
            log.info("Tentando enviar email para artista: {}", emailTrimmed);
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
            log.info("Email enviado com sucesso para artista: {}", emailTrimmed);
        } catch (Exception e) {
            log.error("Erro ao enviar email para artista: {}", e.getMessage(), e);
        }

        // Enviar notificação para equipe InkFlow
        try {
            log.info("Tentando enviar notificação para equipe InkFlow");
            SimpleMailMessage mailEquipe = new SimpleMailMessage();
            mailEquipe.setFrom(remetente);
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
            log.info("Notificação enviada com sucesso para equipe InkFlow");
        } catch (Exception e) {
            log.error("Erro ao enviar email para equipe: {}", e.getMessage(), e);
        }
    }
    
    @Async
    public void enviarEmailTeste(String emailDestino) {
        try {
            log.info("Testando envio de email para: {}", emailDestino);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(remetente);
            message.setTo(emailDestino);
            message.setSubject("Teste de Email - InkFlow");
            message.setText("Este é um email de teste do sistema InkFlow.\n\nSe você recebeu esta mensagem, o sistema de email está funcionando corretamente!");
            mailSender.send(message);
            log.info("Email de teste enviado com sucesso para: {}", emailDestino);
        } catch (Exception e) {
            log.error("Erro ao enviar email de teste: {}", e.getMessage(), e);
        }
    }
}
