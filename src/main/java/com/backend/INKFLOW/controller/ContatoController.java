package com.backend.INKFLOW.controller;

import com.backend.INKFLOW.dto.ContatoRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/contato")
public class ContatoController {

    private static final Logger log = LoggerFactory.getLogger(ContatoController.class);

    @Autowired
    private JavaMailSender mailSender;

    @PostMapping
    public ResponseEntity<?> enviarContato(@Valid @RequestBody ContatoRequest request) {
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
            log.error("Erro ao enviar email de contato: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Erro ao enviar e-mail."));
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(e -> e instanceof FieldError ? ((FieldError) e).getDefaultMessage() : e.getDefaultMessage())
                .orElse("Dados inválidos.");
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }
}
