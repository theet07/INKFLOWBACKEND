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
        return new BCryptPasswordEncoder();
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
                .requestMatchers("/api/auth/**", "/ping", "/api/health").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/clientes").permitAll()

                // Rotas exclusivas de ADMIN
                .requestMatchers("/api/diagnostic/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/clientes").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/admins").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/agendamentos/{id}").hasAnyRole("ADMIN")

                // Rotas exclusivas de ARTISTA ou ADMIN
                .requestMatchers(HttpMethod.GET, "/api/agendamentos").hasAnyRole("ARTISTA", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/agendamentos/artista/{artistaId}").hasAnyRole("ARTISTA", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/agendamentos/status/{status}").hasAnyRole("ARTISTA", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/agendamentos/{id}").hasAnyRole("ARTISTA", "ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/agendamentos/{id}/status").hasAnyRole("ARTISTA", "ADMIN", "CLIENTE")

                // Rotas de cliente autenticado
                .requestMatchers(HttpMethod.GET, "/api/clientes/email/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/clientes/*/foto").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/clientes/*/foto").authenticated()

                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
