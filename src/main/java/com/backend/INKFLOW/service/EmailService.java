package com.backend.INKFLOW.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remetente;

    public void enviarCodigoVerificacao(String destinatario, String codigo) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(remetente);
            message.setTo(destinatario);
            message.setSubject("InkFlow — Seu código de verificação");
            message.setText(
                "Olá!\n\n" +
                "Seu código de verificação para criar sua conta no InkFlow é:\n\n" +
                "  " + codigo + "\n\n" +
                "Este código expira em 15 minutos.\n" +
                "Se você não solicitou este cadastro, ignore este e-mail.\n\n" +
                "— Equipe InkFlow"
            );
            mailSender.send(message);
            log.info("Codigo de verificacao enviado para: {}", destinatario);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail para {}: {}", destinatario, e.getMessage());
            throw new RuntimeException("Falha ao enviar e-mail de verificacao. Tente novamente.");
        }
    }
}
