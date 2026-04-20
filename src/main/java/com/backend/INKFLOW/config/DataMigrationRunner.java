package com.backend.INKFLOW.config;

import com.backend.INKFLOW.model.Cliente;
import com.backend.INKFLOW.repository.ClienteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Migração de dados executada automaticamente no boot da aplicação.
 * Detecta senhas em texto plano (sem prefixo $2a$) e aplica hash BCrypt.
 * Idempotente: senhas já hasheadas são ignoradas.
 */
@Component
public class DataMigrationRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataMigrationRunner.class);

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("[Migration] Verificando senhas em texto plano...");

        List<Cliente> clientes = clienteRepository.findAll();
        int convertidas = 0;
        int ignoradas = 0;

        for (Cliente cliente : clientes) {
            String senha = cliente.getPassword();

            if (senha == null || senha.isBlank()) {
                ignoradas++;
                continue;
            }

            if (senha.startsWith("$2a$") || senha.startsWith("$2b$") || senha.startsWith("$2y$")) {
                ignoradas++;
                continue;
            }

            // Senha em texto plano — aplica hash e salva
            cliente.setPassword(passwordEncoder.encode(senha));
            clienteRepository.save(cliente);
            convertidas++;
        }

        if (convertidas > 0) {
            log.info("[Migration] Concluida: {} senha(s) convertida(s) para BCrypt. {} ja estavam hasheadas.",
                    convertidas, ignoradas);
        } else {
            log.info("[Migration] Nenhuma senha em texto plano encontrada. {} registro(s) verificado(s).", ignoradas);
        }
    }
}
