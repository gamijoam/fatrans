package com.tufondo.auth.infrastructure.security;

import com.tufondo.auth.infrastructure.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Extrae token desde Authorization header")
    void extrae_token_desde_header() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid_token");
        when(jwtService.esAccessTokenValido("valid_token")).thenReturn(true);
        when(jwtService.extraerUsuarioId("valid_token")).thenReturn(java.util.UUID.randomUUID());
        when(jwtService.extraerRol("valid_token")).thenReturn("ADMIN");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    @Test
    @DisplayName("Extrae token desde cookie access_token")
    void extrae_token_desde_cookie() throws Exception {
        Cookie cookie = new Cookie("access_token", "cookie_token");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(request.getHeader("Authorization")).thenReturn(null);
        when(jwtService.esAccessTokenValido("cookie_token")).thenReturn(true);
        when(jwtService.extraerUsuarioId("cookie_token")).thenReturn(java.util.UUID.randomUUID());
        when(jwtService.extraerRol("cookie_token")).thenReturn("ADMIN");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    @Test
    @DisplayName("No autentica si no hay token")
    void no_autentica_si_no_hay_token() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("No filtra rutas de autenticación")
    void no_filtra_rutas_auth() {
        when(request.getServletPath()).thenReturn("/api/v1/auth/login");

        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    @Test
    @DisplayName("Si filtra rutas protegidas")
    void si_filtra_rutas_protegidas() {
        when(request.getServletPath()).thenReturn("/api/v1/socios");

        assertThat(filter.shouldNotFilter(request)).isFalse();
    }
}