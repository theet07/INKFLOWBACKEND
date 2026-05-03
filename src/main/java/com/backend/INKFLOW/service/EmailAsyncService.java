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

import java.util.concurrent.CompletableFuture;

@Service
public class EmailAsyncService {
    private static final Logger log = LoggerFactory.getLogger(EmailAsyncService.class);
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 2000;
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String remetente;
    
    public void enviarEmailsLead(LeadArtistaRequest request, String emailTrimmed, String whatsappLimpo) {
        // Usar CompletableFuture para executar em background mantendo contexto
        CompletableFuture.runAsync(() -> {
            enviarComRetry(emailTrimmed, () -> {
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
            });
        });
    }
    
    private void enviarComRetry(String destinatario, Runnable sendAction) {
        for (int tentativa = 1; tentativa <= MAX_RETRIES; tentativa++) {
            try {
                log.info("[ASYNC] Tentativa {}/{} - Enviando para: {}", tentativa, MAX_RETRIES, destinatario);
                sendAction.run();
                log.info("[ASYNC] Email enviado com sucesso para: {}", destinatario);
                return;
            } catch (Exception e) {
                log.warn("[ASYNC] Tentativa {}/{} falhou: {}", tentativa, MAX_RETRIES, e.getMessage());
                if (tentativa < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS * tentativa);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("[ASYNC] Thread interrompida durante retry");
                        return;
                    }
                } else {
                    log.error("[ASYNC] Todas as tentativas falharam para: {}", destinatario, e);
                }
            }
        }
    }
    
    public void enviarEmailTeste(String emailDestino) {
        CompletableFuture.runAsync(() -> {
            enviarComRetry(emailDestino, () -> {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(remetente);
                message.setTo(emailDestino);
                message.setSubject("Teste de Email - InkFlow");
                message.setText("Este é um email de teste do sistema InkFlow.\n\nSe você recebeu esta mensagem, o sistema de email está funcionando corretamente!");
                mailSender.send(message);
            });
        });
    }
}
