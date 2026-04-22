// com/tufondo/beneficiarios/infrastructure/security/BeneficiarioRateLimitFilter.java
package com.tufondo.beneficiarios.infrastructure.security;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Filtro de Rate Limiting para endpoints de beneficiarios.
 * 🔒 SECURITY: Previene IP spoofing usando solo X-Real-IP o remoteAddr,
 * ignorando X-Forwarded-For que puede ser manipulado.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class BeneficiarioRateLimitFilter extends OncePerRequestFilter {

    private static final int LIMITE_POST_PUT_DELETE = 10;
    private static final int LIMITE_GET = 30;
    private static final Duration VENTANA = Duration.ofMinutes(1);

    private static final Map<String, Bucket> BUCKETS_POST_PUT_DELETE = new ConcurrentHashMap<>();
    private static final Map<String, Bucket> BUCKETS_GET = new ConcurrentHashMap<>();

    private static final Pattern PATTERN_BENEFICIARIOS = Pattern.compile(
            "/api/v1/socios/([a-f0-9\\-]+)/beneficiarios(?:/([a-f0-9\\-]+))?"
    );

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String uri = request.getRequestURI();
        String method = request.getMethod();

        Matcher matcher = PATTERN_BENEFICIARIOS.matcher(uri);
        if (!matcher.matches()) {
            filterChain.doFilter(request, response);
            return;
        }

        String socioId = matcher.group(1);
        String clientIp = getSecureClientIp(request);
        String bucketKey = socioId + ":" + clientIp;

        boolean isWriteOperation = "POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method);
        Bucket bucket = isWriteOperation
                ? BUCKETS_POST_PUT_DELETE.computeIfAbsent(bucketKey, this::createBucketWrite)
                : BUCKETS_GET.computeIfAbsent(bucketKey, this::createBucketRead);

        int limite = isWriteOperation ? LIMITE_POST_PUT_DELETE : LIMITE_GET;

        if (!bucket.tryConsume(1)) {
            log.warn("Rate limit excedido para beneficiario. SocioId: {}, IP: {}, Método: {}, URI: {}",
                    socioId, clientIp, method, uri);
            enviarError429(response, limite);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 🔒 SECURITY: Obtiene IP del cliente de forma segura.
     * Solo usa X-Real-IP (header de proxy confiable) o remoteAddr.
     * NO usa X-Forwarded-For que puede ser manipulado por atacantes.
     */
    private String getSecureClientIp(HttpServletRequest request) {
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && isTrustedProxyHeader(xRealIp)) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    /**
     * 🔒 SECURITY: Valida que el header X-Real-IP venga de un proxy confiable.
     * En producción esto debería verificar contra lista blanca de IPs de proxy.
     */
    private boolean isTrustedProxyHeader(String ip) {
        return ip != null && !ip.isEmpty()
                && !ip.contains(",")
                && ip.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");
    }

    private Bucket createBucketWrite(String key) {
        Refill refill = Refill.greedy(LIMITE_POST_PUT_DELETE, VENTANA);
        return Bucket.builder()
                .addLimit(Bandwidth.classic(LIMITE_POST_PUT_DELETE, refill))
                .build();
    }

    private Bucket createBucketRead(String key) {
        Refill refill = Refill.greedy(LIMITE_GET, VENTANA);
        return Bucket.builder()
                .addLimit(Bandwidth.classic(LIMITE_GET, refill))
                .build();
    }

    private void enviarError429(HttpServletResponse response, int limite) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", "60");
        response.getWriter().write(String.format(
                "{\"codigo\":\"RATE_LIMIT_EXCEDIDO\",\"mensaje\":\"Demasiadas solicitudes. Máximo %d por minuto.\",\"retryAfter\":60}",
                limite
        ));
    }
}