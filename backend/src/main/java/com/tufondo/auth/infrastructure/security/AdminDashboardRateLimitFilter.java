package com.tufondo.auth.infrastructure.security;

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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class AdminDashboardRateLimitFilter extends OncePerRequestFilter {

    private static final int REQUESTS_PER_MINUTE = 30;
    private static final long BUCKET_EXPIRATION_MINUTES = 10;
    private static final Map<String, BucketEntry> BUCKETS = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    static {
        scheduler.scheduleAtFixedRate(() -> {
            long now = Instant.now().toEpochMilli();
            BUCKETS.entrySet().removeIf(entry -> {
                if (now - entry.getValue().createdAt > BUCKET_EXPIRATION_MINUTES * 60 * 1000) {
                    return true;
                }
                return false;
            });
            if (!BUCKETS.isEmpty()) {
                log.debug("Rate limit buckets cleaned. Active buckets: {}", BUCKETS.size());
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    private record BucketEntry(Bucket bucket, long createdAt) {}

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if (!request.getRequestURI().equals("/api/v1/admin/dashboard/estadisticas")) {
            filterChain.doFilter(request, response);
            return;
        }

        String rateLimitKey = getRateLimitKey(request);
        BucketEntry entry = BUCKETS.computeIfAbsent(rateLimitKey, this::createBucketEntry);
        Bucket bucket = entry.bucket;

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("Rate limit excedido - endpoint=/api/v1/admin/dashboard/estadisticas");
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", "60");
            response.getWriter().write(
                    "{\"codigo\":\"RATE_LIMIT_EXCEDIDO\",\"mensaje\":\"Demasiadas solicitudes. Intente nuevamente en 60 segundos.\"}"
            );
        }
    }

    private BucketEntry createBucketEntry(String key) {
        Refill refill = Refill.greedy(REQUESTS_PER_MINUTE, Duration.ofMinutes(1));
        Bucket bucket = Bucket.builder()
                .addLimit(Bandwidth.classic(REQUESTS_PER_MINUTE, refill))
                .build();
        return new BucketEntry(bucket, Instant.now().toEpochMilli());
    }

    private String getRateLimitKey(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return "user:" + auth.getName();
        }
        return "ip:" + extractClientIp(request);
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