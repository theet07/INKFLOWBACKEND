package com.backend.INKFLOW.controller;

import com.backend.INKFLOW.model.Cliente;
import com.backend.INKFLOW.model.NotificacaoPreferencia;
import com.backend.INKFLOW.repository.ClienteRepository;
import com.backend.INKFLOW.repository.NotificacaoPreferenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/notificacoes")
public class NotificacaoController {

    @Autowired
    private NotificacaoPreferenciaRepository notifRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    /** GET /api/notificacoes/usuario/{usuarioId} */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> getPreferencias(@PathVariable Long usuarioId) {
        Optional<NotificacaoPreferencia> prefOpt = notifRepository.findByClienteId(usuarioId);

        if (prefOpt.isPresent()) {
            return ResponseEntity.ok(prefOpt.get());
        }

        // Criar preferências padrão se não existir
        Optional<Cliente> clienteOpt = clienteRepository.findById(usuarioId);
        if (clienteOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        NotificacaoPreferencia nova = new NotificacaoPreferencia();
        nova.setCliente(clienteOpt.get());
        notifRepository.save(nova);
        return ResponseEntity.ok(nova);
    }

    /** PUT /api/notificacoes/usuario/{usuarioId} */
    @PutMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> atualizarPreferencias(
            @PathVariable Long usuarioId,
            @RequestBody Map<String, Object> body) {

        Optional<NotificacaoPreferencia> prefOpt = notifRepository.findByClienteId(usuarioId);
        NotificacaoPreferencia pref;

        if (prefOpt.isPresent()) {
            pref = prefOpt.get();
        } else {
            Optional<Cliente> clienteOpt = clienteRepository.findById(usuarioId);
            if (clienteOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            pref = new NotificacaoPreferencia();
            pref.setCliente(clienteOpt.get());
        }

        if (body.containsKey("horarioManha")) pref.setHorarioManha((String) body.get("horarioManha"));
        if (body.containsKey("horarioTarde")) pref.setHorarioTarde((String) body.get("horarioTarde"));
        if (body.containsKey("horarioNoite")) pref.setHorarioNoite((String) body.get("horarioNoite"));
        if (body.containsKey("notificacoesAtivas")) pref.setNotificacoesAtivas((Boolean) body.get("notificacoesAtivas"));

        notifRepository.save(pref);
        return ResponseEntity.ok(pref);
    }
}
