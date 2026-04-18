package com.backend.INKFLOW.service;

import com.backend.INKFLOW.model.Cliente;
import com.backend.INKFLOW.repository.ClienteRepository;
import com.backend.INKFLOW.repository.AgendamentoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class ClienteService {
    
    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @PersistenceContext
    private EntityManager entityManager;
    
    public List<Cliente> getAllClientes() {
        return clienteRepository.findAll();
    }
    
    public Optional<Cliente> getClienteById(Long id) {
        return clienteRepository.findById(id);
    }
    
    public Optional<Cliente> getClienteByUsername(String username) {
        return clienteRepository.findByUsername(username);
    }
    
    public Cliente saveCliente(Cliente cliente) {
        if (cliente.getId() == null && cliente.getPassword() != null
                && !cliente.getPassword().startsWith("$2a$")) {
            cliente.setPassword(passwordEncoder.encode(cliente.getPassword()));
        }
        return clienteRepository.save(cliente);
    }
    
    public boolean existsByUsername(String username) {
        return clienteRepository.existsByUsername(username);
    }
    
    public boolean existsByEmail(String email) {
        return clienteRepository.existsByEmail(email);
    }
    
    @Transactional
    public void deleteCliente(Long id) {
        entityManager.createNativeQuery("DELETE FROM agendamentos WHERE cliente_id = :id")
                .setParameter("id", id)
                .executeUpdate();
        entityManager.createNativeQuery("DELETE FROM clientes WHERE id = :id")
                .setParameter("id", id)
                .executeUpdate();
    }
    
    public Optional<Cliente> getUserByEmail(String email) {
        return clienteRepository.findByEmail(email);
    }

    /** Gera um codigo OTP de 6 digitos, zera tentativas e salva. */
    public String gerarEsalvarCodigo(Cliente cliente) {
        String codigo = String.format("%06d", new Random().nextInt(999999));
        cliente.setCodigoVerificacao(codigo);
        cliente.setContaVerificada(false);
        cliente.setTentativasOtp(0);
        clienteRepository.save(cliente);
        System.out.println("[OTP] Codigo gerado para " + cliente.getEmail() + ": " + codigo);
        return codigo;
    }

    /**
     * Verifica o codigo OTP com rate limiting de 5 tentativas.
     * Retorna:
     *   Optional.of(cliente) — sucesso
     *   Optional.empty()     — codigo errado ou nulo
     *   Lanca TooManyAttemptsException — limite atingido (429)
     */
    public Optional<Cliente> verificarCodigo(String email, String codigo) {
        Optional<Cliente> clienteOpt = clienteRepository.findByEmail(email);

        if (clienteOpt.isEmpty()) {
            System.out.println("[OTP] Email nao encontrado no banco: " + email);
            return Optional.empty();
        }

        Cliente cliente = clienteOpt.get();
        String codigoBanco = cliente.getCodigoVerificacao();
        String codigoRecebido = codigo != null ? codigo.trim() : null;

        System.out.println("[OTP] Email: " + email);
        System.out.println("[OTP] Codigo recebido: '" + codigoRecebido + "'");
        System.out.println("[OTP] Codigo no banco:  '" + codigoBanco + "'");
        System.out.println("[OTP] Tentativas: " + cliente.getTentativasOtp());

        // Verifica limite de tentativas
        if (cliente.getTentativasOtp() >= 5) {
            System.out.println("[OTP] Limite de tentativas atingido para: " + email);
            throw new TooManyOtpAttemptsException("Limite de tentativas excedido. Solicite um novo codigo.");
        }

        if (codigoRecebido == null || codigoBanco == null || !codigoRecebido.equals(codigoBanco)) {
            // Incrementa tentativas e salva
            cliente.setTentativasOtp(cliente.getTentativasOtp() + 1);
            clienteRepository.save(cliente);
            System.out.println("[OTP] Falha: codigo incorreto. Tentativas: " + cliente.getTentativasOtp());
            return Optional.empty();
        }

        // Sucesso: ativa conta e zera tudo
        cliente.setContaVerificada(true);
        cliente.setCodigoVerificacao(null);
        cliente.setTentativasOtp(0);
        Cliente salvo = clienteRepository.save(cliente);
        System.out.println("[OTP] Sucesso: conta verificada para " + email);
        return Optional.of(salvo);
    }

    /** Excecao interna para sinalizar limite de tentativas OTP (429). */
    public static class TooManyOtpAttemptsException extends RuntimeException {
        public TooManyOtpAttemptsException(String message) { super(message); }
    }
}