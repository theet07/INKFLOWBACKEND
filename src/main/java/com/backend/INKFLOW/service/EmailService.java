package com.backend.INKFLOW.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private BrevoService brevoService;

    @Value("${brevo.sender.email}")
    private String remetente;

    public void enviarCodigoVerificacao(String destinatario, String codigo) {
        String texto =
            "Olá!\n\n" +
            "Seu código de verificação para criar sua conta no InkFlow é:\n\n" +
            "  " + codigo + "\n\n" +
            "Este código expira em 15 minutos.\n" +
            "Se você não solicitou este cadastro, ignore este e-mail.\n\n" +
            "— Equipe InkFlow";
        brevoService.enviar(destinatario, "InkFlow — Seu código de verificação", texto);
        log.info("Codigo de verificacao enviado para: {}", destinatario);
    }

    public void enviarEmailConfirmacaoLead(String destinatario, String nomeCompleto, String nomeEstudio, String especialidade, String whatsapp) {
        try {
            String texto =
                "Olá, " + nomeCompleto + "!\n\n" +
                "Recebemos sua solicitação para se tornar um artista parceiro do InkFlow.\n\n" +
                "Dados recebidos:\n" +
                "• Estúdio: " + nomeEstudio + "\n" +
                "• Especialidade: " + especialidade + "\n" +
                "• WhatsApp: " + whatsapp + "\n\n" +
                "Nossa equipe irá analisar sua solicitação e retornaremos em breve via WhatsApp ou email.\n\n" +
                "Enquanto isso, siga-nos no Instagram @inkflowstudios para novidades!\n\n" +
                "Atenciosamente,\nEquipe InkFlow";
            brevoService.enviar(destinatario, "Recebemos sua solicitação - InkFlow", texto);
            log.info("Email de confirmacao de lead enviado para: {}", destinatario);
        } catch (Exception e) {
            log.error("Falha ao enviar email de confirmacao para {}: {}", destinatario, e.getMessage());
        }
    }

    public void enviarBackupEmail(String conteudo, String filename) {
        try {
            String texto = "Backup gerado com sucesso.\n\nArquivo: " + filename +
                "\nGerado em: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            brevoService.enviarComAnexo(remetente, "InkFlow — Backup Manual: " + filename, texto,
                filename, conteudo.getBytes(StandardCharsets.UTF_8));
            log.info("Backup enviado por email: {}", filename);
        } catch (Exception e) {
            log.error("Falha ao enviar backup por email: {}", e.getMessage(), e);
        }
    }
}
