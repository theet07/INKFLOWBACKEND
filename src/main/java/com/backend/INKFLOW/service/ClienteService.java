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

    /** Gera um codigo OTP de 6 digitos, salva no cliente e retorna o codigo. */
    public String gerarEsalvarCodigo(Cliente cliente) {
        String codigo = String.format("%06d", new Random().nextInt(999999));
        cliente.setCodigoVerificacao(codigo);
        cliente.setContaVerificada(false);
        clienteRepository.save(cliente);
        return codigo;
    }

    /**
     * Verifica o codigo OTP. Se correto, ativa a conta e limpa o codigo.
     * Retorna o cliente ativado ou empty se o codigo for invalido.
     */
    public Optional<Cliente> verificarCodigo(String email, String codigo) {
        return clienteRepository.findByEmail(email).map(cliente -> {
            if (codigo != null && codigo.equals(cliente.getCodigoVerificacao())) {
                cliente.setContaVerificada(true);
                cliente.setCodigoVerificacao(null);
                return clienteRepository.save(cliente);
            }
            return null;
        });
    }
}