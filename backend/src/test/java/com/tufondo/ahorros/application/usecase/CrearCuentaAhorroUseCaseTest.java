package com.tufondo.ahorros.application.usecase;

import com.tufondo.ahorros.application.dto.CreateCuentaAhorroRequest;
import com.tufondo.ahorros.application.dto.CuentaAhorroResponse;
import com.tufondo.ahorros.application.mapper.AhorrosDTOMapper;
import com.tufondo.ahorros.domain.model.CuentaAhorro;
import com.tufondo.ahorros.domain.model.enums.Moneda;
import com.tufondo.ahorros.domain.model.enums.TipoCuenta;
import com.tufondo.ahorros.domain.repository.CuentaAhorroRepository;

import static org.mockito.ArgumentMatchers.eq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests para issue #179: anti-IDOR en crear cuenta de ahorro.
 *
 * <p>Antes del fix, {@code CrearCuentaAhorroUseCase.ejecutar(request)} no
 * validaba ownership del {@code socioId} del request contra el token JWT.
 * Un socio A podía crear cuentas para socio B simplemente poniendo el ID
 * de B en el body.</p>
 */
@ExtendWith(MockitoExtension.class)
class CrearCuentaAhorroUseCaseTest {

    @Mock
    private CuentaAhorroRepository cuentaRepository;

    @Mock
    private AhorrosDTOMapper mapper;

    @InjectMocks
    private CrearCuentaAhorroUseCase useCase;

    private CreateCuentaAhorroRequest request;
    private UUID socioRequestId;

    @BeforeEach
    void setUp() {
        socioRequestId = UUID.randomUUID();
        request = new CreateCuentaAhorroRequest(
                socioRequestId,
                TipoCuenta.AHORRO,
                Moneda.VES,
                new BigDecimal("100.00"),
                new BigDecimal("0.05")
        );
    }

    @Test
    @DisplayName("Issue #179: socio creando cuenta para SÍ MISMO (mismo UUID) pasa")
    void socio_crea_cuenta_para_si_mismo_pasa() {
        CuentaAhorro cuentaMock = mock(CuentaAhorro.class);
        CuentaAhorroResponse responseMock = mock(CuentaAhorroResponse.class);
        when(cuentaRepository.existePorSocioIdYTipo(any(), any())).thenReturn(false);
        when(cuentaRepository.existePorNumeroCuenta(anyString())).thenReturn(false);
        when(cuentaRepository.guardar(any())).thenReturn(cuentaMock);
        when(mapper.toDomain(any(CreateCuentaAhorroRequest.class), anyString())).thenReturn(cuentaMock);
        when(mapper.toResponse(any(CuentaAhorro.class))).thenReturn(responseMock);

        // socioIdToken == request.socioId → ownership válido
        CuentaAhorroResponse result = useCase.ejecutar(request, socioRequestId, false);

        assertThat(result).isNotNull();
        verify(cuentaRepository).guardar(any());
    }

    @Test
    @DisplayName("Issue #179: socio intentando crear cuenta para OTRO socio → AccessDeniedException (anti-IDOR)")
    void socio_crea_cuenta_para_otro_socio_falla() {
        UUID otroSocio = UUID.randomUUID();

        // socioIdToken (otroSocio) != request.socioId → IDOR detectado
        assertThatThrownBy(() -> useCase.ejecutar(request, otroSocio, false))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("No tiene permisos");

        // CRÍTICO: el repositorio NUNCA debe ser invocado (cuenta NO se crea)
        verify(cuentaRepository, never()).existePorSocioIdYTipo(any(), any());
        verify(cuentaRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("Issue #179: ADMIN puede crear cuenta para cualquier socio (back-office)")
    void admin_crea_cuenta_para_cualquier_socio_pasa() {
        UUID adminSinSocioId = UUID.randomUUID();  // admin no es socio
        CuentaAhorro cuentaMock = mock(CuentaAhorro.class);
        CuentaAhorroResponse responseMock = mock(CuentaAhorroResponse.class);
        when(cuentaRepository.existePorSocioIdYTipo(any(), any())).thenReturn(false);
        when(cuentaRepository.existePorNumeroCuenta(anyString())).thenReturn(false);
        when(cuentaRepository.guardar(any())).thenReturn(cuentaMock);
        when(mapper.toDomain(any(CreateCuentaAhorroRequest.class), anyString())).thenReturn(cuentaMock);
        when(mapper.toResponse(any(CuentaAhorro.class))).thenReturn(responseMock);

        // isAdmin = true → skip ownership check, permite crear para cualquier socio
        CuentaAhorroResponse result = useCase.ejecutar(request, adminSinSocioId, true);

        assertThat(result).isNotNull();
        verify(cuentaRepository).guardar(any());
    }

    @Test
    @DisplayName("Issue #179: token sin socioId (null) y no-admin → AccessDeniedException")
    void sin_socioId_y_no_admin_falla() {
        // Caso edge: token de un usuario que no tiene socio asociado (ej. error de
        // sistema, token corrupto) y NO es admin → debe bloquear por seguridad.
        assertThatThrownBy(() -> useCase.ejecutar(request, null, false))
                .isInstanceOf(AccessDeniedException.class);

        verify(cuentaRepository, never()).guardar(any());
    }
}
