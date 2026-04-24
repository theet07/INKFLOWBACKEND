package com.backend.INKFLOW.controller;

import com.backend.INKFLOW.model.Mensagem;
import com.backend.INKFLOW.repository.AgendamentoRepository;
import com.backend.INKFLOW.repository.MensagemRepository;
import com.backend.INKFLOW.service.ArtistaService;
import com.backend.INKFLOW.service.ClienteService;
import com.backend.INKFLOW.service.ChatRateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mensagens")
public class MensagemController {

    @Autowired private MensagemRepository mensagemRepository;
    @Autowired private AgendamentoRepository agendamentoRepository;
    @Autowired private ClienteService clienteService;
    @Autowired private ArtistaService artistaService;
    @Autowired private ChatRateLimitService rateLimitService;

    @PostMapping
    public ResponseEntity<?> enviar(@RequestBody Map<String, Object> body,
                                     Authentication auth,
                                     HttpServletRequest request) {
        // Rate limiting por IP
        String ip = getClientIp(request);
        if (!rateLimitService.isAllowed(ip)) {
            return ResponseEntity.status(429).body(Map.of("message", "Muitas mensagens. Aguarde um momento."));
        }

        Object remIdRaw = body.get("remetenteId");
        Object destIdRaw = body.get("destinatarioId");
        String conteudo = (String) body.get("conteudo");

        if (remIdRaw == null || destIdRaw == null || conteudo == null || conteudo.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Campos obrigatórios: remetenteId, destinatarioId, conteudo."));
        }

        Long remetenteId = ((Number) remIdRaw).longValue();
        Long destinatarioId = ((Number) destIdRaw).longValue();

        // Validar ownership: remetenteId deve bater com o usuário do token
        Long userIdDoToken = resolveUserId(auth);
        if (userIdDoToken == null || !userIdDoToken.equals(remetenteId)) {
            return ResponseEntity.status(403).body(Map.of("message", "Ação não permitida."));
        }

        // Validar tamanho
        if (conteudo.length() > 500) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mensagem muito longa."));
        }

        // Validar relação de agendamento entre os dois usuários
        boolean temRelacao = verificarRelacao(remetenteId, destinatarioId);
        if (!temRelacao) {
            return ResponseEntity.status(403).body(Map.of("message", "Você só pode conversar com artistas com quem agendou."));
        }

        // Sanitizar
        String conteudoLimpo = conteudo
            .replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "")
            .replaceAll("<[^>]*>", "")
            .strip();

        Mensagem msg = new Mensagem();
        msg.setRemetenteId(remetenteId);
        msg.setDestinatarioId(destinatarioId);
        msg.setConteudo(conteudoLimpo);

        return ResponseEntity.ok(mensagemRepository.save(msg));
    }

    @GetMapping("/conversa/{outroUsuarioId}")
    public ResponseEntity<?> conversa(@PathVariable Long outroUsuarioId, Authentication auth) {
        Long userIdDoToken = resolveUserId(auth);
        if (userIdDoToken == null) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(mensagemRepository.findConversa(userIdDoToken, outroUsuarioId));
    }

    @GetMapping("/novas")
    public ResponseEntity<?> novas(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
                                    Authentication auth) {
        Long userIdDoToken = resolveUserId(auth);
        if (userIdDoToken == null) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(mensagemRepository.findNovas(userIdDoToken, desde));
    }

    @PatchMapping("/{id}/lida")
    public ResponseEntity<?> marcarLida(@PathVariable Long id, Authentication auth) {
        Long userIdDoToken = resolveUserId(auth);
        return mensagemRepository.findById(id).map(msg -> {
            if (!msg.getDestinatarioId().equals(userIdDoToken)) {
                return ResponseEntity.status(403).body(Map.of("message", "Acesso negado."));
            }
            msg.setLida(true);
            return ResponseEntity.ok(mensagemRepository.save(msg));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/nao-lidas/count")
    public ResponseEntity<?> countNaoLidas(Authentication auth) {
        Long userIdDoToken = resolveUserId(auth);
        if (userIdDoToken == null) return ResponseEntity.status(403).build();
        long total = mensagemRepository.countByDestinatarioIdAndLidaFalse(userIdDoToken);
        return ResponseEntity.ok(Map.of("total", total));
    }

    @GetMapping("/conversas")
    public ResponseEntity<?> conversas(Authentication auth) {
        Long userIdDoToken = resolveUserId(auth);
        if (userIdDoToken == null) return ResponseEntity.status(403).build();
        List<Long> remetentes = mensagemRepository.findRemetentesDoArtista(userIdDoToken);
        // Enriquecer com dados do cliente
        List<Map<String, Object>> resultado = remetentes.stream().map(cid -> {
            Map<String, Object> info = new java.util.LinkedHashMap<>();
            info.put("clienteId", cid);
            clienteService.getClienteById(cid).ifPresent(c -> {
                info.put("nome", c.getFullName());
                info.put("fotoUrl", c.getProfileImage());
            });
            return info;
        }).toList();
        return ResponseEntity.ok(resultado);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Long resolveUserId(Authentication auth) {
        String email = auth.getName();
        // Tenta como cliente primeiro, depois como artista
        var cliente = clienteService.getUserByEmail(email);
        if (cliente.isPresent()) return cliente.get().getId();
        var artista = artistaService.getByEmail(email);
        if (artista.isPresent()) return artista.get().getId().longValue();
        return null;
    }

    private boolean verificarRelacao(Long remetenteId, Long destinatarioId) {
        // cliente → artista
        boolean c2a = agendamentoRepository.existsByClienteIdAndArtistaId(remetenteId, destinatarioId.intValue());
        // artista → cliente
        boolean a2c = agendamentoRepository.existsByArtistaIdAndClienteId(remetenteId.intValue(), destinatarioId);
        return c2a || a2c;
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}
