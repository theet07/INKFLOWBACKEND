package com.backend.INKFLOW;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GerarHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        String senha = "Matheus&NathanS2";
        String hash = encoder.encode(senha);
        System.out.println("HASH: " + hash);
        System.out.println("VALIDO: " + encoder.matches(senha, hash));
    }
}
