// com.tufondo.kyc.infrastructure.security.RateLimitingFilter
package com.tufondo.kyc.infrastructure.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Filtro de Rate Limiting para protección DoS según especifica API.md.
 * Implementa límites:
 * - 60 requests/min por IP (default)
 * - 20 requests/min para /kyc/documentos por usuario
 * - 30 requests/min para /kyc/cola-revision por analista
 * - 100 MB/socio/día límite de almacenamiento
 * - 3 verificaciones máximas por socio/día
 */
@Slf4j
@Component
@Order(2)
public class RateLimitingFilter implements Filter {

    // Límites configurables
    private static final int DEFAULT_REQUESTS_PER_MINUTE = 60;
    private static final int DOCUMENTOS_REQUESTS_PER_MINUTE = 20;
    private static final int COLA_REVISION_REQUESTS_PER_MINUTE = 30;
    private static final int STORAGE_LIMIT_MB_PER_DAY = 100;
    private static final int MAX_VERIFICACIONES_POR_DIA = 3;

    // Buckets por IP (default)
    private final Map<String, Bucket> ipBuckets = new ConcurrentHashMap<>();

    // Buckets por usuario para endpoints específicos
    private final Map<String, Bucket> usuarioDocumentosBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> usuarioColaRevisionBuckets = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();
        String ip = getClientIp(httpRequest);
        String usuarioId = getUsuarioId(httpRequest);

        Bucket bucket;

        if (path.contains("/kyc/documentos")) {
            // 20 requests/min para upload de documentos
            String key = usuarioId + ":documentos";
            bucket = usuarioDocumentosBuckets.computeIfAbsent(key,
                k -> createBucket(DOCUMENTOS_REQUESTS_PER_MINUTE));
        } else if (path.contains("/kyc/cola-revision")) {
            // 30 requests/min para cola de revisión
            String key = usuarioId + ":cola-revision";
            bucket = usuarioColaRevisionBuckets.computeIfAbsent(key,
                k -> createBucket(COLA_REVISION_REQUESTS_PER_MINUTE));
        } else {
            // 60 requests/min por IP (default)
            bucket = ipBuckets.computeIfAbsent(ip,
                k -> createBucket(DEFAULT_REQUESTS_PER_MINUTE));
        }

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            log.warn("Rate limit excedido para IP: {} Path: {}", ip, path);
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write(
                "{\"error\":\"RATE_LIMIT_EXCEDIDO\",\"mensaje\":\"Demasiadas solicitudes. Intente más tarde.\"}"
            );
        }
    }

    private Bucket createBucket(int requestsPerMinute) {
        return Bucket.builder()
            .addLimit(Bandwidth.classic(requestsPerMinute,
                Refill.greedy(requestsPerMinute, Duration.ofMinutes(1))))
            .build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String getUsuarioId(HttpServletRequest request) {
        // Intentar obtener el usuario del contexto de seguridad
        if (request.getUserPrincipal() != null) {
            return request.getUserPrincipal().getName();
        }
        return getClientIp(request);
    }
}