package com.backend.INKFLOW;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@org.springframework.scheduling.annotation.EnableScheduling
public class InkflowApplication {

	private static final Logger log = LoggerFactory.getLogger(InkflowApplication.class);

	public static void main(String[] args) {
		try {
			SpringApplication.run(InkflowApplication.class, args);
		} catch (Exception e) {
			log.error("[STARTUP ERROR] {}: {}", e.getClass().getSimpleName(), e.getMessage());
			if (e.getCause() != null) log.error("[CAUSA] {}", e.getCause().getMessage());
			throw e;
		}
	}

	@GetMapping("/ping")
	public String ping() {
		return "pong";
	}
}
