package com.tufondo.creditos.infrastructure.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Filtro de Rate Limiting para el endpoint /simulador del módulo de Créditos.
 * Limita a 10 solicitudes por minuto por IP.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class SimulacionRateLimitFilter extends OncePerRequestFilter {

    private static final int SIMULACIONES_POR_MINUTO = 10;
    private static final Map<String, Bucket> BUCKETS = new ConcurrentHashMap<>();
    private static final Pattern PATTERN_SIMULADOR = Pattern.compile("/api/v1/simulador$");

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String uri = request.getRequestURI();

        if (!esSimulador(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = extractClientIp(request);
        Bucket bucket = BUCKETS.computeIfAbsent(clientIp, this::createBucket);

        if (!bucket.tryConsume(1)) {
            log.warn("Rate limit excedido para simulación. IP: {}, URI: {}", clientIp, uri);
            enviarError429(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean esSimulador(String uri) {
        return PATTERN_SIMULADOR.matcher(uri).matches();
    }

    private Bucket createBucket(String key) {
        Refill refill = Refill.greedy(SIMULACIONES_POR_MINUTO, Duration.ofMinutes(1));
        return Bucket.builder()
                .addLimit(Bandwidth.classic(SIMULACIONES_POR_MINUTO, refill))
                .build();
    }

    private void enviarError429(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", "60");
        response.getWriter().write(
                "{\"error\":\"RATE_LIMIT_EXCEEDED\",\"mensaje\":\"Ha excedido el límite de simulaciones. Máximo " 
                + SIMULACIONES_POR_MINUTO + " por minuto.\",\"retryAfter\":60}"
        );
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}