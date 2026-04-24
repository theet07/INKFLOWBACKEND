package com.backend.INKFLOW.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("https://inkflowfrontend.vercel.app", "http://localhost:*", "https://*.app.github.dev", "https://*.vercel.app"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/ping", "/api/health", "/api/status", "/").permitAll()
                .requestMatchers("/api/chat").authenticated()
                .requestMatchers("/api/mensagens/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/clientes").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/clientes/solicitar-codigo").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/clientes/verificar-codigo").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/appointments").permitAll()
                // Rotas /api/artists (alias ingles) - publicas
                .requestMatchers(HttpMethod.GET, "/api/artists").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/artists/**").permitAll()
                // Rotas v1 da Landing Page - publicas
                .requestMatchers(HttpMethod.GET, "/api/v1/artists/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/appointments").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/appointments").hasAnyRole("ARTISTA", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/artistas").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/artistas/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/portfolio/artista/*").permitAll()
                .requestMatchers("/api/portfolio/**").hasAnyRole("ARTISTA", "ADMIN")
                // Disponibilidade: GET publico, escrita restrita a ARTISTA/ADMIN
                .requestMatchers(HttpMethod.GET, "/api/disponibilidade/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/disponibilidade/**").hasAnyRole("ARTISTA", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/disponibilidade/**").hasAnyRole("ARTISTA", "ADMIN")

                // Rotas exclusivas de ADMIN
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/admins").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/clientes").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/agendamentos/{id}").hasRole("ADMIN")
                // ARTISTA ou ADMIN
                // #8 — ADMIN apenas: artista nao deve ver todos os agendamentos do sistema
                .requestMatchers(HttpMethod.GET, "/api/agendamentos").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/agendamentos/artista/{artistaId}").hasAnyRole("ARTISTA", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/agendamentos/status/{status}").hasAnyRole("ARTISTA", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/agendamentos/{id}").hasAnyRole("ARTISTA", "ADMIN")
                // #9 — role desconhecida cai aqui sem fallback; validacao de role esta no service
                .requestMatchers(HttpMethod.PATCH, "/api/agendamentos/{id}/status").hasAnyRole("ARTISTA", "ADMIN", "CLIENTE")
                // #7 — avaliacao restrita ao CLIENTE: artista e admin nao podem avaliar
                .requestMatchers(HttpMethod.PUT, "/api/appointments/{id}/avaliar").hasRole("CLIENTE")
                // Perfil do artista
                .requestMatchers(HttpMethod.PUT, "/api/artistas/{id}").hasRole("ARTISTA")
                .requestMatchers(HttpMethod.PUT, "/api/artists/{id}").hasRole("ARTISTA")
                .requestMatchers(HttpMethod.POST, "/api/artistas/{id}/foto").hasRole("ARTISTA")
                .requestMatchers(HttpMethod.POST, "/api/artists/{id}/foto").hasRole("ARTISTA")

                // Rotas de cliente autenticado
                .requestMatchers(HttpMethod.GET, "/api/appointments/meus").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/appointments/cliente/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/clientes/email/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/clientes/*/foto").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/clientes/*/foto").authenticated()

                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
