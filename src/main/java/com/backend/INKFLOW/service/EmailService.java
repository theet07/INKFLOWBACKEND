package com.backend.INKFLOW.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    public void enviarEmailConfirmacaoLead(String destinatario, String nomeCompleto, String nomeEstudio, String especialidade, String whatsapp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(remetente);
            message.setTo(destinatario);
            message.setSubject("Recebemos sua solicitação - InkFlow 🎨");
            message.setText(
                "Olá, " + nomeCompleto + "!\n\n" +
                "Recebemos sua solicitação para se tornar um artista parceiro do InkFlow.\n\n" +
                "Dados recebidos:\n" +
                "• Estúdio: " + nomeEstudio + "\n" +
                "• Especialidade: " + especialidade + "\n" +
                "• WhatsApp: " + whatsapp + "\n\n" +
                "Nossa equipe irá analisar sua solicitação e retornaremos em breve via WhatsApp ou email.\n\n" +
                "Enquanto isso, siga-nos no Instagram @inkflowstudios para novidades!\n\n" +
                "Atenciosamente,\n" +
                "Equipe InkFlow"
            );
            mailSender.send(message);
            log.info("Email de confirmacao de lead enviado para: {}", destinatario);
        } catch (Exception e) {
            log.error("Falha ao enviar email de confirmacao para {}: {}", destinatario, e.getMessage());
        }
    }

    public void enviarBackupEmail(String conteudo, String filename) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(remetente);
            helper.setTo(remetente);
            helper.setSubject("InkFlow — Backup Automático: " + filename);
            helper.setText("Backup automático gerado com sucesso.\n\nArquivo: " + filename +
                    "\nGerado em: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            helper.addAttachment(filename, new ByteArrayResource(conteudo.getBytes(StandardCharsets.UTF_8)));
            mailSender.send(message);
            log.info("Backup enviado por email: {}", filename);
        } catch (Exception e) {
            log.error("Falha ao enviar backup por email: {}", e.getMessage(), e);
        }
    }
}
