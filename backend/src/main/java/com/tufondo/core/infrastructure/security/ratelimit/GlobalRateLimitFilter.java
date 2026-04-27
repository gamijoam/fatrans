package com.tufondo.core.infrastructure.security.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class GlobalRateLimitFilter extends OncePerRequestFilter {

    private final RedisRateLimitingService rateLimitingService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Map<String, RateLimitConfig> ENDPOINT_LIMITS;
    static {
        Map<String, RateLimitConfig> limits = new java.util.HashMap<>();
        limits.put("/api/v1/auth/login", new RateLimitConfig(5, Duration.ofMinutes(1)));
        limits.put("/api/v1/auth/registro", new RateLimitConfig(3, Duration.ofMinutes(1)));
        limits.put("/api/v1/auth/recuperar-password", new RateLimitConfig(3, Duration.ofMinutes(1)));
        limits.put("/api/v1/auth/reset-password", new RateLimitConfig(3, Duration.ofMinutes(1)));
        limits.put("/api/v1/admin/**", new RateLimitConfig(30, Duration.ofMinutes(1)));
        limits.put("/api/v1/perfil/**", new RateLimitConfig(10, Duration.ofMinutes(1)));
        limits.put("/api/v1/socios/**", new RateLimitConfig(60, Duration.ofMinutes(1)));
        limits.put("/api/v1/cuentas/**", new RateLimitConfig(30, Duration.ofMinutes(1)));
        limits.put("/api/v1/creditos/simular", new RateLimitConfig(10, Duration.ofMinutes(1)));
        limits.put("/api/v1/creditos/**", new RateLimitConfig(30, Duration.ofMinutes(1)));
        limits.put("/api/v1/documentos/**", new RateLimitConfig(20, Duration.ofMinutes(1)));
        ENDPOINT_LIMITS = Map.copyOf(limits);
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        RateLimitConfig config = findMatchingConfig(path);
        if (config == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = extractClientIp(request);
        String rateLimitKey = buildRateLimitKey(clientIp, path);

        RedisRateLimitingService.RateLimitResult result = rateLimitingService.checkRateLimit(
                rateLimitKey, config.limit(), config.window()
        );

        response.setHeader("X-RateLimit-Limit", String.valueOf(result.limit()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(result.remaining()));

        if (!result.allowed()) {
            log.warn("Rate limit exceeded for IP: {} on path: {}", clientIp, path);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            Map<String, Object> errorResponse = Map.of(
                    "codigo", "RATE_LIMIT_EXCEDIDO",
                    "mensaje", "Demasiadas solicitudes. Intente nuevamente en " + config.window().getSeconds() + " segundos.",
                    "retryAfter", config.window().getSeconds()
            );

            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private RateLimitConfig findMatchingConfig(String path) {
        for (Map.Entry<String, RateLimitConfig> entry : ENDPOINT_LIMITS.entrySet()) {
            String pattern = entry.getKey();
            if (matchesPattern(path, pattern)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private boolean matchesPattern(String path, String pattern) {
        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 3);
            return path.startsWith(prefix);
        }
        return path.equals(pattern);
    }

    private String buildRateLimitKey(String clientIp, String path) {
        return "rate_limit:" + clientIp + ":" + path;
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

    private record RateLimitConfig(int limit, Duration window) {}
}