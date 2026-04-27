package com.backend.INKFLOW.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Order(1)
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private final Map<String, BucketEntry> buckets = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private static class BucketEntry {
        final Bucket bucket;
        long lastAccess;

        BucketEntry(Bucket bucket) {
            this.bucket = bucket;
            this.lastAccess = System.currentTimeMillis();
        }
    }

    @PostConstruct
    public void init() {
        // Limpa buckets inativos a cada 30 minutos
        scheduler.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            long ttl = TimeUnit.HOURS.toMillis(1); // Remove após 1h de inatividade
            buckets.entrySet().removeIf(entry -> (now - entry.getValue().lastAccess) > ttl);
        }, 30, 30, TimeUnit.MINUTES);
    }

    @PreDestroy
    public void destroy() {
        scheduler.shutdown();
    }

    private Bucket getBucket(String ip) {
        BucketEntry entry = buckets.computeIfAbsent(ip, k -> 
            new BucketEntry(Bucket.builder()
                .addLimit(Bandwidth.builder()
                    .capacity(5)
                    .refillIntervally(5, Duration.ofMinutes(15))
                    .build())
                .build())
        );
        entry.lastAccess = System.currentTimeMillis();
        return entry.bucket;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
                                    throws ServletException, IOException {

        if (!request.getRequestURI().equals("/api/auth/login")) {
            chain.doFilter(request, response);
            return;
        }

        String ip = getClientIp(request);
        Bucket bucket = getBucket(ip);

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                "{\"success\": false, \"message\": \"Muitas tentativas. Aguarde 15 minutos.\"}"
            );
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
