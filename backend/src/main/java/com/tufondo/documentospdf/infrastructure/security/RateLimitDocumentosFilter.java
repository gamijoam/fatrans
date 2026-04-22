// com.tufondo.documentospdf.infrastructure.security.RateLimitDocumentosFilter
package com.tufondo.documentospdf.infrastructure.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Filtro de Rate Limiting para el módulo de Documentos.
 *
 * Límites:
 * - 5 req/min por usuario para GENERACIÓN de documentos
 * - 20 req/min por IP para GENERACIÓN
 * - 10 req/min por usuario para DESCARGA de documentos
 * - 30 req/min por usuario para CONSULTA (listar, obtener metadata)
 *
 * Además valida IP contra spoofing usando X-Forwarded-For.
 */
@Slf4j
@Component
@Order(2)
public class RateLimitDocumentosFilter implements Filter {

    // Buckets por usuario (userId) para generación
    private final Map<String, Bucket> userBucketsGen = new ConcurrentHashMap<>();

    // Buckets por IP para generación
    private final Map<String, Bucket> ipBucketsGen = new ConcurrentHashMap<>();

    // Buckets por usuario para descarga
    private final Map<String, Bucket> userBucketsDownload = new ConcurrentHashMap<>();

    // Buckets por usuario para consulta
    private final Map<String, Bucket> userBucketsQuery = new ConcurrentHashMap<>();

    // IPs confiables (proxies/load balancers)
    private static final List<String> TRUSTED_PROXIES = List.of(
            "127.0.0.1", "localhost", "::1"
    );

    @Value("${documentospdf.rate-limit.generation-per-user-per-minute:5}")
    private int genPorUsuario;

    @Value("${documentospdf.rate-limit.generation-per-ip-per-minute:20}")
    private int genPorIp;

    @Value("${documentospdf.rate-limit.download-per-user-per-minute:10}")
    private int downloadPorUsuario;

    @Value("${documentospdf.rate-limit.query-per-user-per-minute:30}")
    private int queryPorUsuario;

    @Value("${server.trusted-proxies:}")
    private String trustedProxies;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();

        // Solo aplicar rate limiting a endpoints de documentos
        if (!path.startsWith("/api/v1/documentos")) {
            chain.doFilter(request, response);
            return;
        }

        // Clasificar operación y validar rate limit
        OperationType opType = classifyOperation(path, method);
        String userId = extractUserId(httpRequest);
        String clientIp = getValidatedClientIp(httpRequest);

        try {
            switch (opType) {
                case GENERACION -> handleGeneracion(httpRequest, httpResponse, userId, clientIp, chain);
                case DESCARGA -> handleDescarga(httpRequest, httpResponse, userId, chain);
                case CONSULTA -> handleConsulta(httpRequest, httpResponse, userId, chain);
                case OTHER -> chain.doFilter(request, response);
            }
        } catch (Exception e) {
            log.error("Error en RateLimitFilter", e);
            chain.doFilter(request, response);
        }
    }

    private OperationType classifyOperation(String path, String method) {
        // POST siempre es generación
        if ("POST".equals(method)) {
            return OperationType.GENERACION;
        }

        // Descarga explícita
        if (path.contains("/descargar")) {
            return OperationType.DESCARGA;
        }

        // Endpoints de generación de documentos (reconocidos por tener un ID al final)
        // GET /documentos/estado-cuenta/{id}
        // GET /documentos/constancia-afiliacion/{id}
        // GET /documentos/contrato/{id}
        // GET /documentos/pagare/{id}
        // GET /documentos/tabla-amortizacion/{id}
        // GET /documentos/carta-beneficiarios/{id}
        if (method.equals("GET") && containsDocumentTypeId(path)) {
            return OperationType.GENERACION;
        }

        // Listar u obtener metadata
        // GET /documentos/{documentoId}
        // GET /documentos/socio/{socioId}
        if (method.equals("GET")) {
            return OperationType.CONSULTA;
        }

        return OperationType.OTHER;
    }

    private boolean containsDocumentTypeId(String path) {
        return path.contains("/estado-cuenta/") ||
               path.contains("/constancia-afiliacion/") ||
               path.contains("/contrato/") ||
               path.contains("/pagare/") ||
               path.contains("/tabla-amortizacion/") ||
               path.contains("/carta-beneficiarios/");
    }

    private void handleGeneracion(HttpServletRequest request, HttpServletResponse response,
                                  String userId, String clientIp, FilterChain chain) throws IOException, ServletException {

        // Rate limit por usuario: 5 req/min
        Bucket userBucket = userBucketsGen.computeIfAbsent(
                userId, k -> createGenUserBucket());
        if (!userBucket.tryConsume(1)) {
            log.warn("Rate limit de USUARIO excedido para GENERACIÓN: userId={}, path={}",
                    userId, request.getRequestURI());
            sendRateLimitResponse(response, "Generación de documentos por usuario", 5);
            return;
        }

        // Rate limit por IP: 20 req/min
        Bucket ipBucket = ipBucketsGen.computeIfAbsent(
                clientIp, k -> createGenIpBucket());
        if (!ipBucket.tryConsume(1)) {
            log.warn("Rate limit de IP excedido para GENERACIÓN: ip={}, path={}",
                    clientIp, request.getRequestURI());
            sendRateLimitResponse(response, "Generación de documentos por IP", 20);
            return;
        }

        log.debug("Rate limit GENERACIÓN OK: userId={}, ip={}", userId, clientIp);
        chain.doFilter(request, response);
    }

    private void handleDescarga(HttpServletRequest request, HttpServletResponse response,
                                 String userId, FilterChain chain) throws IOException, ServletException {

        Bucket bucket = userBucketsDownload.computeIfAbsent(
                userId, k -> createDownloadBucket());
        if (!bucket.tryConsume(1)) {
            log.warn("Rate limit excedido para DESCARGA: userId={}, path={}",
                    userId, request.getRequestURI());
            sendRateLimitResponse(response, "Descarga de documentos", 10);
            return;
        }

        log.debug("Rate limit DESCARGA OK: userId={}", userId);
        chain.doFilter(request, response);
    }

    private void handleConsulta(HttpServletRequest request, HttpServletResponse response,
                                String userId, FilterChain chain) throws IOException, ServletException {

        Bucket bucket = userBucketsQuery.computeIfAbsent(
                userId, k -> createQueryBucket());
        if (!bucket.tryConsume(1)) {
            log.warn("Rate limit excedido para CONSULTA: userId={}, path={}",
                    userId, request.getRequestURI());
            sendRateLimitResponse(response, "Consulta de documentos", 30);
            return;
        }

        log.debug("Rate limit CONSULTA OK: userId={}", userId);
        chain.doFilter(request, response);
    }

    private Bucket createGenUserBucket() {
        Bandwidth limit = Bandwidth.classic(genPorUsuario,
                Refill.intervally(genPorUsuario, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket createGenIpBucket() {
        Bandwidth limit = Bandwidth.classic(genPorIp,
                Refill.intervally(genPorIp, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket createDownloadBucket() {
        Bandwidth limit = Bandwidth.classic(downloadPorUsuario,
                Refill.intervally(downloadPorUsuario, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket createQueryBucket() {
        Bandwidth limit = Bandwidth.classic(queryPorUsuario,
                Refill.intervally(queryPorUsuario, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    private void sendRateLimitResponse(HttpServletResponse response, String tipo, int limite) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.setHeader("Retry-After", "60");
        response.getWriter().write(String.format(
                "{\"codigo\":\"DOC_010\",\"mensaje\":\"Rate limit excedido para %s. Límite: %d req/min. Intente nuevamente en 60 segundos.\",\"retryAfter\":60}",
                tipo, limite));
    }

    /**
     * Extrae el userId del principal de Spring Security.
     */
    private String extractUserId(HttpServletRequest request) {
        if (request.getUserPrincipal() != null) {
            return request.getUserPrincipal().getName();
        }
        return "anonymous";
    }

    /**
     * Obtiene la IP del cliente validando contra IP spoofing.
     *
     * Si la solicitud viene de un proxy de confianza, usa X-Forwarded-For.
     * Si NO viene de un proxy de confianza, IGNORA estos headers para evitar spoofing.
     */
    private String getValidatedClientIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        boolean isFromTrustedProxy = TRUSTED_PROXIES.contains(remoteAddr);

        // Si viene de proxy de confianza, acepta X-Forwarded-For
        if (isFromTrustedProxy) {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                // X-Forwarded-For puede contener múltiples IPs: client, proxy1, proxy2
                // Solo tomar la primera (IP real del cliente)
                String clientIp = xForwardedFor.split(",")[0].trim();

                // Validar que la IP del header sea válida
                if (isValidIp(clientIp)) {
                    log.debug("IP validada via X-Forwarded-For: {} (proxy: {})", clientIp, remoteAddr);
                    return clientIp;
                }
            }

            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty() && isValidIp(xRealIp)) {
                log.debug("IP validada via X-Real-IP: {} (proxy: {})", xRealIp, remoteAddr);
                return xRealIp;
            }
        } else {
            // NO viene de proxy de confianza - posibles ataques de IP spoofing
            // Ignorar headers X-Forwarded-For y X-Real-IP
            log.debug("Solicitud directa (no proxy), ignorando headers de forwarded: remoteAddr={}",
                    remoteAddr);
        }

        return remoteAddr;
    }

    /**
     * Validación básica de formato de IP.
     */
    private boolean isValidIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        // IPv4 básica o IPv6
        return ip.matches("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$") ||
               ip.matches("^[0-9a-fA-F:]+$");
    }

    private enum OperationType {
        GENERACION,
        DESCARGA,
        CONSULTA,
        OTHER
    }
}
