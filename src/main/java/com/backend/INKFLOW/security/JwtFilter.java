package com.backend.INKFLOW.security;

import com.backend.INKFLOW.repository.TokenBlacklistRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired private JwtUtil jwtUtil;
    @Autowired private TokenBlacklistRepository tokenBlacklistRepository;

    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            String header = request.getHeader("Authorization");

            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                if (jwtUtil.validateToken(token)) {
                    String jti = jwtUtil.extractJti(token);
                    if (tokenBlacklistRepository.existsByTokenId(jti)) {
                        log.warn("Token revogado (blacklist) para URI: {}", request.getRequestURI());
                        chain.doFilter(request, response);
                        return;
                    }

                    String email = jwtUtil.extractEmail(token);
                    String role = jwtUtil.extractRole(token);

                    if (role == null || role.isBlank()) {
                        log.warn("Token valido mas sem role definida para o email: {}", email);
                        chain.doFilter(request, response);
                        return;
                    }

                    var authority = new SimpleGrantedAuthority(role);
                    var auth = new UsernamePasswordAuthenticationToken(email, null, List.of(authority));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } else {
                    log.warn("Token invalido ou expirado para URI: {}", request.getRequestURI());
                }
            }

            chain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Erro no JwtFilter [{}] {}: {}", request.getMethod(), request.getRequestURI(), e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"" + e.getClass().getSimpleName() + "\",\"message\":\"" + e.getMessage() + "\"}");
        }
    }
}
