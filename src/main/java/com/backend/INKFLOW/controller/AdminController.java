package com.backend.INKFLOW.controller;

import com.backend.INKFLOW.model.Admin;
import com.backend.INKFLOW.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/admins")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping
    public ResponseEntity<?> createAdmin(@RequestBody Admin admin) {
        if (adminService.getByEmail(admin.getEmail()).isPresent()) {
            return ResponseEntity.status(409).body(Map.of("message", "Email já cadastrado."));
        }
        return ResponseEntity.ok(adminService.save(admin));
    }
}
