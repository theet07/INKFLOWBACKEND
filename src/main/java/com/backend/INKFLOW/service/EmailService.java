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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remetente;

    @Value("${resend.api.key:}")
    private String resendApiKey;

    public void enviarCodigoVerificacao(String destinatario, String codigo) {
        try {
            if (resendApiKey != null && !resendApiKey.trim().isEmpty()) {
                enviarViaBrevo(destinatario, "InkFlow — Seu código de verificação", "Seu código é: " + codigo);
                log.info("Codigo de verificacao enviado via Resend API para: {}", destinatario);
                return;
            }

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
            log.info("Codigo de verificacao enviado via SMTP para: {}", destinatario);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail para {}: {}", destinatario, e.getMessage());
            throw new RuntimeException("Falha ao enviar e-mail de verificacao. Tente novamente.");
        }
    }

    public void enviarCodigoRecuperacaoSenha(String destinatario, String codigo, String nomeCliente) {
        try {
            if (resendApiKey != null && !resendApiKey.trim().isEmpty()) {
                enviarViaBrevo(destinatario, "InkFlow — Recuperação de Senha", "Seu código de recuperação é: " + codigo);
                log.info("Codigo de recuperacao de senha enviado via Resend API para: {}", destinatario);
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(remetente);
            message.setTo(destinatario);
            message.setSubject("InkFlow — Recuperação de Senha");
            message.setText(
                "Olá" + (nomeCliente != null ? ", " + nomeCliente : "") + "!\n\n" +
                "Recebemos uma solicitação para redefinir sua senha no InkFlow.\n\n" +
                "Seu código de recuperação é:\n\n" +
                "  " + codigo + "\n\n" +
                "Este código expira em 15 minutos.\n" +
                "Se você não solicitou esta recuperação, ignore este e-mail e sua senha permanecerá inalterada.\n\n" +
                "— Equipe InkFlow"
            );
            mailSender.send(message);
            log.info("Codigo de recuperacao de senha enviado para: {}", destinatario);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail de recuperacao para {}: {}", destinatario, e.getMessage());
            throw new RuntimeException("Falha ao enviar e-mail de recuperacao. Tente novamente.");
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

    private void enviarViaBrevo(String destinatario, String assunto, String texto) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", resendApiKey); // Usando a mesma variável pra você não precisar recriar no Render, apenas substitua o valor
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        
        Map<String, String> sender = new HashMap<>();
        sender.put("email", remetente); // O e-mail que você cadastrou no Gmail
        sender.put("name", "InkFlow App");
        body.put("sender", sender);
        
        Map<String, String> to = new HashMap<>();
        to.put("email", destinatario);
        body.put("to", Collections.singletonList(to));
        
        body.put("subject", assunto);
        body.put("htmlContent", "<p>" + texto.replace("\n", "<br>") + "</p>");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        
        try {
            restTemplate.postForObject("https://api.brevo.com/v3/smtp/email", request, String.class);
        } catch (Exception e) {
            log.error("Erro na API do Brevo: {}", e.getMessage());
            throw new RuntimeException("Falha ao enviar e-mail via Brevo.");
        }
    }
}
