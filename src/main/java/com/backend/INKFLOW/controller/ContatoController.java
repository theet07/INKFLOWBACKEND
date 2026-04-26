package com.backend.INKFLOW.controller;

import com.backend.INKFLOW.dto.ContatoRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/contato")
public class ContatoController {

    @Autowired
    private JavaMailSender mailSender;

    @PostMapping
    public ResponseEntity<?> enviarContato(@RequestBody ContatoRequest request) {
        if (request.getNome() == null || request.getEmail() == null || request.getMensagem() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Campos obrigatórios faltando."));
        }
        if (request.getMensagem().length() > 2000) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mensagem muito longa."));
        }

        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo("inkflowstudios07@gmail.com");
            mail.setSubject("Novo contato via InkFlow — " + request.getNome());
            mail.setText(
                "Nome: " + request.getNome() + "\n" +
                "E-mail: " + request.getEmail() + "\n" +
                "Telefone: " + (request.getTelefone() != null ? request.getTelefone() : "Não informado") + "\n\n" +
                "Mensagem:\n" + request.getMensagem()
            );
            mail.setReplyTo(request.getEmail());
            mailSender.send(mail);
            return ResponseEntity.ok(Map.of("message", "Mensagem enviada com sucesso!"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erro ao enviar e-mail."));
        }
    }
}
