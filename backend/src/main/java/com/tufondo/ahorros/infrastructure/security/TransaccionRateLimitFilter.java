package com.tufondo.ahorros.infrastructure.security;

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

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class TransaccionRateLimitFilter extends OncePerRequestFilter {

    private static final int TRANSACCIONES_POR_MINUTO = 30;
    private static final int CONSULTA_POR_MINUTO = 60;
    private static final Map<String, Bucket> BUCKETS_TRANSACCION = new ConcurrentHashMap<>();
    private static final Map<String, Bucket> BUCKETS_CONSULTA = new ConcurrentHashMap<>();

    private static final Pattern PATTERN_DEPOSITO = Pattern.compile("/api/v1/cuentas/[^/]+/depositos$");
    private static final Pattern PATTERN_RETIRO = Pattern.compile("/api/v1/cuentas/[^/]+/retiros$");
    private static final Pattern PATTERN_MOVIMIENTOS = Pattern.compile("/api/v1/cuentas/[^/]+/movimientos");
    private static final Pattern PATTERN_SALDO = Pattern.compile("/api/v1/cuentas/[^/]+/saldo$");
    private static final Pattern PATTERN_RENDIMIENTOS = Pattern.compile("/api/v1/cuentas/[^/]+/rendimientos");

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String uri = request.getRequestURI();
        String clientIp = extractClientIp(request);

        if (esTransaccion(uri)) {
            Bucket bucket = BUCKETS_TRANSACCION.computeIfAbsent(clientIp, this::createBucketTransaccion);
            if (!bucket.tryConsume(1)) {
                log.warn("Rate limit excedido para transacción. IP: {}, URI: {}", clientIp, uri);
                enviarError429(response, "Ha excedido el límite de transacciones. Máximo " + TRANSACCIONES_POR_MINUTO + " por minuto");
                return;
            }
        } else if (esConsulta(uri)) {
            Bucket bucket = BUCKETS_CONSULTA.computeIfAbsent(clientIp, this::createBucketConsulta);
            if (!bucket.tryConsume(1)) {
                log.warn("Rate limit excedido para consulta. IP: {}, URI: {}", clientIp, uri);
                enviarError429(response, "Ha excedido el límite de consultas. Máximo " + CONSULTA_POR_MINUTO + " por minuto");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean esTransaccion(String uri) {
        return PATTERN_DEPOSITO.matcher(uri).matches() || PATTERN_RETIRO.matcher(uri).matches();
    }

    private boolean esConsulta(String uri) {
        return PATTERN_MOVIMIENTOS.matcher(uri).matches() 
            || PATTERN_SALDO.matcher(uri).matches() 
            || PATTERN_RENDIMIENTOS.matcher(uri).matches();
    }

    private Bucket createBucketTransaccion(String key) {
        Refill refill = Refill.greedy(TRANSACCIONES_POR_MINUTO, Duration.ofMinutes(1));
        return Bucket.builder()
                .addLimit(Bandwidth.classic(TRANSACCIONES_POR_MINUTO, refill))
                .build();
    }

    private Bucket createBucketConsulta(String key) {
        Refill refill = Refill.greedy(CONSULTA_POR_MINUTO, Duration.ofMinutes(1));
        return Bucket.builder()
                .addLimit(Bandwidth.classic(CONSULTA_POR_MINUTO, refill))
                .build();
    }

    private void enviarError429(HttpServletResponse response, String mensaje) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", "60");
        response.getWriter().write(
                "{\"codigo\":\"RATE_LIMIT_EXCEDIDO\",\"mensaje\":\"" + mensaje + "\"}"
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
