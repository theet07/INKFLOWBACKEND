package com.backend.INKFLOW.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

@Service
public class BrevoService {

    private static final Logger log = LoggerFactory.getLogger(BrevoService.class);
    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name:InkFlow}")
    private String senderName;

    private final RestTemplate restTemplate = new RestTemplate();

    public void enviar(String para, String assunto, String texto) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            String body = String.format(
                "{\"sender\":{\"name\":\"%s\",\"email\":\"%s\"}," +
                "\"to\":[{\"email\":\"%s\"}]," +
                "\"subject\":\"%s\"," +
                "\"textContent\":\"%s\"}",
                senderName, senderEmail, para,
                assunto, texto.replace("\"", "\\\"").replace("\n", "\\n")
            );

            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(BREVO_API_URL, entity, String.class);
            log.info("Email enviado via Brevo para: {}", para);
        } catch (Exception e) {
            log.error("Falha ao enviar email via Brevo para {}: {}", para, e.getMessage());
            throw new RuntimeException("Falha ao enviar e-mail.", e);
        }
    }

    public void enviarComAnexo(String para, String assunto, String texto, String filename, byte[] conteudo) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            String base64 = Base64.getEncoder().encodeToString(conteudo);
            String body = String.format(
                "{\"sender\":{\"name\":\"%s\",\"email\":\"%s\"}," +
                "\"to\":[{\"email\":\"%s\"}]," +
                "\"subject\":\"%s\"," +
                "\"textContent\":\"%s\"," +
                "\"attachment\":[{\"name\":\"%s\",\"content\":\"%s\"}]}",
                senderName, senderEmail, para,
                assunto, texto.replace("\"", "\\\"").replace("\n", "\\n"),
                filename, base64
            );

            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(BREVO_API_URL, entity, String.class);
            log.info("Email com anexo enviado via Brevo para: {}", para);
        } catch (Exception e) {
            log.error("Falha ao enviar email com anexo via Brevo: {}", e.getMessage());
            throw new RuntimeException("Falha ao enviar e-mail com anexo.", e);
        }
    }
}
