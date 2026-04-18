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
		SpringApplication.run(InkflowApplication.class, args);
	}

	@GetMapping("/ping")
	public String ping() {
		return "pong";
	}
}
