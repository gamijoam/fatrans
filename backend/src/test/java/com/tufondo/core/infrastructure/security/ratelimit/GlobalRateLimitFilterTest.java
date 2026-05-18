package com.tufondo.core.infrastructure.security.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests del fix G4: la regla de rate-limit del registro apuntaba a
 * /api/v1/auth/registro (inexistente). Debe aplicarse a
 * /api/v1/socios/solicitud con 3 req/min.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalRateLimitFilter - Reglas y prioridad de matching")
class GlobalRateLimitFilterTest {

    @Mock
    private RedisRateLimitingService rateLimitingService;

    @Mock
    private FilterChain filterChain;

    private GlobalRateLimitFilter filter;

    @BeforeEach
    void setUp() {
        filter = new GlobalRateLimitFilter(rateLimitingService);
    }

    @Test
    @DisplayName("3 requests a /api/v1/socios/solicitud pasan; la 4ta retorna 429")
    void registroBloqueaCuartaRequest() throws Exception {
        // Stub: el servicio devuelve "allowed" hasta el 3er request, luego bloquea.
        AtomicInteger contador = new AtomicInteger(0);
        when(rateLimitingService.checkRateLimit(any(), anyInt(), any()))
                .thenAnswer(inv -> {
                    int intento = contador.incrementAndGet();
                    int limit = inv.getArgument(1);
                    if (intento <= limit) {
                        return new RedisRateLimitingService.RateLimitResult(true, limit, limit - intento);
                    }
                    return new RedisRateLimitingService.RateLimitResult(false, limit, 0);
                });

        // 3 requests permitidas
        for (int i = 0; i < 3; i++) {
            MockHttpServletRequest req = newRequest("/api/v1/socios/solicitud");
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilter(req, res, filterChain);
            assertThat(res.getStatus()).isEqualTo(200);
        }

        // 4ta — debe ser 429
        MockHttpServletRequest req = newRequest("/api/v1/socios/solicitud");
        MockHttpServletResponse res = new MockHttpServletResponse();
        filter.doFilter(req, res, filterChain);

        assertThat(res.getStatus()).isEqualTo(429);
        assertThat(res.getContentAsString()).contains("RATE_LIMIT_EXCEDIDO");
        // El filterChain solo se invoca para las primeras 3 (las permitidas).
        verify(filterChain, times(3)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    @DisplayName("La regla específica /api/v1/socios/solicitud usa límite=3 (no 60 de socios/**)")
    void reglaEspecificaPrevaleceSobreWildcard() throws Exception {
        when(rateLimitingService.checkRateLimit(any(), anyInt(), any()))
                .thenReturn(new RedisRateLimitingService.RateLimitResult(true, 3, 2));

        MockHttpServletRequest req = newRequest("/api/v1/socios/solicitud");
        MockHttpServletResponse res = new MockHttpServletResponse();
        filter.doFilter(req, res, filterChain);

        // Verificamos que se invocó con limit=3 (estricto del registro)
        // y NO con limit=60 (la regla general /api/v1/socios/**).
        ArgumentCaptor<Integer> limitCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Duration> windowCaptor = ArgumentCaptor.forClass(Duration.class);
        verify(rateLimitingService).checkRateLimit(any(), limitCaptor.capture(), windowCaptor.capture());

        assertThat(limitCaptor.getValue()).isEqualTo(3);
        assertThat(windowCaptor.getValue()).isEqualTo(Duration.ofMinutes(1));
    }

    @Test
    @DisplayName("Requests a /api/v1/socios/{id}/cuentas usan límite genérico de socios/** (60), no el de registro (3)")
    void otrosEndpointsSociosNoUsanLimiteDeRegistro() throws Exception {
        when(rateLimitingService.checkRateLimit(any(), anyInt(), any()))
                .thenReturn(new RedisRateLimitingService.RateLimitResult(true, 60, 59));

        MockHttpServletRequest req = newRequest("/api/v1/socios/123/cuentas");
        MockHttpServletResponse res = new MockHttpServletResponse();
        filter.doFilter(req, res, filterChain);

        ArgumentCaptor<Integer> limitCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(rateLimitingService).checkRateLimit(any(), limitCaptor.capture(), any());

        assertThat(limitCaptor.getValue())
                .as("Sub-rutas de /api/v1/socios/** deben usar 60/min, no el 3/min del registro")
                .isEqualTo(60);
        assertThat(res.getStatus()).isEqualTo(200);
        verify(filterChain).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    @DisplayName("Paths sin regla configurada pasan sin rate-limit")
    void pathSinReglaPasaSinLimite() throws Exception {
        MockHttpServletRequest req = newRequest("/actuator/health");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(req, res, filterChain);

        assertThat(res.getStatus()).isEqualTo(200);
        verify(rateLimitingService, never()).checkRateLimit(any(), anyInt(), any());
        verify(filterChain).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    @DisplayName("X-Forwarded-For se usa como IP del cliente cuando está presente")
    void usaXForwardedForCuandoEstaPresente() throws Exception {
        when(rateLimitingService.checkRateLimit(any(), anyInt(), any()))
                .thenReturn(new RedisRateLimitingService.RateLimitResult(true, 3, 2));

        MockHttpServletRequest req = newRequest("/api/v1/socios/solicitud");
        req.addHeader("X-Forwarded-For", "203.0.113.42, 10.0.0.1");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(req, res, filterChain);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(rateLimitingService).checkRateLimit(keyCaptor.capture(), anyInt(), any());
        assertThat(keyCaptor.getValue()).contains("203.0.113.42");
    }

    private MockHttpServletRequest newRequest(String path) {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI(path);
        req.setMethod("POST");
        req.setRemoteAddr("127.0.0.1");
        return req;
    }
}
