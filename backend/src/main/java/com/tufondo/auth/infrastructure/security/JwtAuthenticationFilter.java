package com.tufondo.auth.infrastructure.security;

import com.tufondo.auth.infrastructure.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ACCESS_TOKEN_COOKIE = "access_token";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String token = extractTokenFromHeader(request);

        if (token == null) {
            token = extractTokenFromCookie(request);
        }

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (jwtService.esAccessTokenValido(token)) {
                UUID userId = jwtService.extraerUsuarioId(token);
                String rol = jwtService.extraerRol(token);
                UUID socioId = jwtService.extraerSocioId(token);

                List<SimpleGrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_" + rol)
                );

                AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                        userId, null, socioId, rol, authorities
                );

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                authenticatedUser,
                                null,
                                authorities
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("Autenticación exitosa para usuario: {}, socioId: {}", userId, socioId);
            }
        } catch (Exception e) {
            log.warn("Error al procesar token JWT: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromHeader(HttpServletRequest request) {
        final String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        return Arrays.stream(cookies)
                .filter(c -> ACCESS_TOKEN_COOKIE.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // Issue #179: /api/v1/auth/crear-usuario REMOVIDO del bypass.
        // Era accesible sin autenticación → cualquier atacante creaba usuarios
        // vinculados a socios existentes. Ahora requiere JWT + rol ADMIN
        // (enforce via @PreAuthorize en AuthController.crearUsuario).
        return path.equals("/api/v1/auth/login")
            || path.equals("/api/v1/auth/login-web")
            || path.equals("/api/v1/auth/logout")
            || path.equals("/api/v1/auth/logout-web")
            || path.equals("/api/v1/auth/refresh")
            || path.equals("/api/v1/auth/refresh-web")
            || path.equals("/api/v1/auth/validar")
            || path.equals("/api/v1/auth/recuperar-password")
            || path.equals("/api/v1/auth/reset-password")
            || path.equals("/api/v1/socios/solicitud");
    }
}
