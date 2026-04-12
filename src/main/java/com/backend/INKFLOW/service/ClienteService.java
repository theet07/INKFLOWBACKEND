package com.backend.INKFLOW.service;

import com.backend.INKFLOW.model.Cliente;
import com.backend.INKFLOW.model.Agendamento;
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
        List<Agendamento> agendamentos = agendamentoRepository.findByClienteId(id);
        if (!agendamentos.isEmpty()) {
            agendamentoRepository.deleteAllInBatch(agendamentos);
        }
        clienteRepository.deleteById(id);
        entityManager.flush();
    }
    
    public Optional<Cliente> getUserByEmail(String email) {
        return clienteRepository.findByEmail(email);
    }
}