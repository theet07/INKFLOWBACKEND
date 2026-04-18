package com.backend.INKFLOW;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@org.springframework.scheduling.annotation.EnableScheduling
public class InkflowApplication {

	public static void main(String[] args) {
		try {
			SpringApplication.run(InkflowApplication.class, args);
		} catch (Exception e) {
			System.err.println("[STARTUP ERROR] " + e.getClass().getSimpleName() + ": " + e.getMessage());
			if (e.getCause() != null) System.err.println("[CAUSA] " + e.getCause().getMessage());
			throw e;
		}
	}

	@GetMapping("/ping")
	public String ping() {
		return "pong";
	}
}
