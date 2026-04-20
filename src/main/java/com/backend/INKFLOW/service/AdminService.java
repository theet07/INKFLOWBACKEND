package com.backend.INKFLOW.service;

import com.backend.INKFLOW.model.Admin;
import com.backend.INKFLOW.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Optional<Admin> getByEmail(String email) {
        return adminRepository.findByEmail(email);
    }

    public Admin save(Admin admin) {
        if (admin.getId() == null && admin.getPassword() != null
                && !admin.getPassword().startsWith("$2a$")) {
            admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        }
        return adminRepository.save(admin);
    }
}
