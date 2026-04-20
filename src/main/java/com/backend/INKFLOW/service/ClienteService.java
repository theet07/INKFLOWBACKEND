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

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ClienteService.class);
    private static final java.security.SecureRandom secureRandom = new java.security.SecureRandom();
    
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
        if (cliente.getPassword() != null && !cliente.getPassword().startsWith("$2a$")) {
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
        String codigo = String.format("%06d", secureRandom.nextInt(1000000));
        cliente.setCodigoVerificacao(codigo);
        cliente.setContaVerificada(false);
        cliente.setTentativasOtp(0);
        clienteRepository.save(cliente);
        log.debug("[OTP] Codigo gerado para {}", cliente.getEmail());
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
            log.debug("[OTP] Email nao encontrado: {}", email);
            return Optional.empty();
        }

        Cliente cliente = clienteOpt.get();
        String codigoBanco = cliente.getCodigoVerificacao();
        String codigoRecebido = codigo != null ? codigo.trim() : null;

        log.debug("[OTP] Verificacao para {} — tentativas: {}", email, cliente.getTentativasOtp());

        if (cliente.getTentativasOtp() >= 5) {
            log.debug("[OTP] Limite atingido para: {}", email);
            throw new TooManyOtpAttemptsException("Limite de tentativas excedido. Solicite um novo codigo.");
        }

        if (codigoRecebido == null || codigoBanco == null || !codigoRecebido.equals(codigoBanco)) {
            cliente.setTentativasOtp(cliente.getTentativasOtp() + 1);
            clienteRepository.save(cliente);
            log.debug("[OTP] Falha para {}. Tentativas: {}", email, cliente.getTentativasOtp());
            return Optional.empty();
        }

        cliente.setContaVerificada(true);
        cliente.setCodigoVerificacao(null);
        cliente.setTentativasOtp(0);
        Cliente salvo = clienteRepository.save(cliente);
        log.debug("[OTP] Sucesso: conta verificada para {}", email);
        return Optional.of(salvo);
    }

    /** Excecao interna para sinalizar limite de tentativas OTP (429). */
    public static class TooManyOtpAttemptsException extends RuntimeException {
        public TooManyOtpAttemptsException(String message) { super(message); }
    }
}