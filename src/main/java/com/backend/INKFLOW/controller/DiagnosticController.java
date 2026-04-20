package com.backend.INKFLOW.controller;

import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
public class DiagnosticController {

    @GetMapping("/api/status")
    public Map<String, String> getApiStatus() {
        return Map.of(
            "status", "ONLINE",
            "message", "API InkFlow esta funcionando",
            "timestamp", java.time.LocalDateTime.now().toString()
        );
    }
}