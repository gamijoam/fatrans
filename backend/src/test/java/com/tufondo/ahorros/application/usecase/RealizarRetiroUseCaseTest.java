package com.tufondo.ahorros.application.usecase;

import com.tufondo.ahorros.application.dto.RetiroRequest;
import com.tufondo.ahorros.application.dto.MovimientoResponse;
import com.tufondo.ahorros.application.mapper.AhorrosDTOMapper;
import com.tufondo.ahorros.application.port.output.AhorrosContabilidadPort;
import com.tufondo.ahorros.domain.exception.AccesoCuentaAjenaException;
import com.tufondo.ahorros.domain.exception.CuentaAhorroNoEncontradaException;
import com.tufondo.ahorros.domain.exception.CuentaNoPermiteOperacionesException;
import com.tufondo.ahorros.domain.exception.SaldoInsuficienteException;
import com.tufondo.ahorros.domain.model.CuentaAhorro;
import com.tufondo.ahorros.domain.model.Movimiento;
import com.tufondo.ahorros.domain.model.enums.CanalOrigen;
import com.tufondo.ahorros.domain.model.enums.EstadoCuenta;
import com.tufondo.ahorros.domain.model.enums.Moneda;
import com.tufondo.ahorros.domain.repository.CuentaAhorroRepository;
import com.tufondo.ahorros.domain.repository.MovimientoRepository;
import com.tufondo.compliance.application.service.LocdoftOperacionService;
import com.tufondo.contabilidad.application.exception.AsientoContableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests del use case de retiro incluyendo el hook contable (#267).
 */
@ExtendWith(MockitoExtension.class)
class RealizarRetiroUseCaseTest {

    @Mock private CuentaAhorroRepository cuentaRepository;
    @Mock private MovimientoRepository movimientoRepository;
    @Mock private AhorrosDTOMapper mapper;
    @Mock private LocdoftOperacionService locdoftService;
    @Mock private AhorrosContabilidadPort contabilidadPort;

    @InjectMocks private RealizarRetiroUseCase useCase;

    private UUID socioId;
    private CuentaAhorro cuentaActiva;
    private RetiroRequest request;

    @BeforeEach
    void setUp() {
        socioId = UUID.randomUUID();
        cuentaActiva = CuentaAhorro.builder()
                .id(UUID.randomUUID())
                .numeroCuenta("AHO-2026-000010")
                .socioId(socioId)
                .saldoActual(new BigDecimal("1000.00"))
                .saldoRetenido(BigDecimal.ZERO)
                .estado(EstadoCuenta.ACTIVA)
                .moneda(Moneda.VES)
                .build();

        request = new RetiroRequest(
                new BigDecimal("200.00"),
                CanalOrigen.WEB,
                null,
                null
        );

        // Mocks por defecto — happy path. Lenient: algunos tests cortan antes
        // (IDOR, saldo insuficiente, cuenta cerrada) y no usan estos stubs.
        lenient().when(cuentaRepository.buscarPorNumeroCuenta("AHO-2026-000010"))
                .thenReturn(Optional.of(cuentaActiva));
        lenient().when(movimientoRepository.sumRetirosDelDiaPorSocio(any(UUID.class), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);
        lenient().when(movimientoRepository.existePorNumeroOperacion(any())).thenReturn(false);
        lenient().when(movimientoRepository.guardar(any())).thenAnswer(inv -> {
            Movimiento m = inv.getArgument(0);
            return Movimiento.builder()
                    .id(UUID.randomUUID())
                    .numeroOperacion(m.getNumeroOperacion())
                    .cuentaAhorroId(m.getCuentaAhorroId())
                    .socioId(m.getSocioId())
                    .tipo(m.getTipo())
                    .monto(m.getMonto())
                    .saldoAnterior(m.getSaldoAnterior())
                    .saldoPosterior(m.getSaldoPosterior())
                    .build();
        });
        lenient().when(mapper.toResponse(any(Movimiento.class)))
                .thenReturn(MovimientoResponse.builder().build());
    }

    // ─── Happy path ────────────────────────────────────────────────────────

    @Test
    @DisplayName("retiro válido invoca al hook contable")
    void retiro_invoca_hook_contable() {
        useCase.ejecutar("AHO-2026-000010", request, socioId, false, "1.1.1.1", "sess", "req");

        verify(contabilidadPort).registrarRetiro(eq(cuentaActiva), any(Movimiento.class));
        verify(movimientoRepository).guardar(any(Movimiento.class));
    }

    @Test
    @DisplayName("saldo se reduce con el monto del retiro")
    void retiro_reduce_saldo() {
        useCase.ejecutar("AHO-2026-000010", request, socioId, false, "1.1.1.1", "sess", "req");

        assertThat(cuentaActiva.getSaldoActual()).isEqualByComparingTo("800.00");
    }

    // ─── Validaciones que cortan ANTES del hook ────────────────────────────

    @Test
    @DisplayName("saldo insuficiente: NO se llega al hook contable")
    void saldo_insuficiente_no_llega_a_hook() {
        request.setMonto(new BigDecimal("5000.00")); // mayor al saldo de 1000

        assertThatThrownBy(() -> useCase.ejecutar(
                "AHO-2026-000010", request, socioId, false, "1.1.1.1", "sess", "req"))
                .isInstanceOf(SaldoInsuficienteException.class);

        verifyNoInteractions(contabilidadPort);
        verify(cuentaRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("cuenta inexistente: NO se llama al hook")
    void cuenta_inexistente_no_llama_hook() {
        when(cuentaRepository.buscarPorNumeroCuenta("AHO-INVALID"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.ejecutar(
                "AHO-INVALID", request, socioId, false, "1.1.1.1", "sess", "req"))
                .isInstanceOf(CuentaAhorroNoEncontradaException.class);

        verifyNoInteractions(contabilidadPort);
    }

    @Test
    @DisplayName("IDOR: socio ajeno NO llega al hook")
    void idor_no_llega_a_hook() {
        UUID otroSocio = UUID.randomUUID();

        assertThatThrownBy(() -> useCase.ejecutar(
                "AHO-2026-000010", request, otroSocio, false, "1.1.1.1", "sess", "req"))
                .isInstanceOf(AccesoCuentaAjenaException.class);

        verifyNoInteractions(contabilidadPort);
    }

    @Test
    @DisplayName("cuenta CERRADA: NO llega al hook")
    void cuenta_cerrada_no_llega_a_hook() {
        CuentaAhorro cerrada = CuentaAhorro.builder()
                .id(cuentaActiva.getId())
                .numeroCuenta("AHO-2026-000010")
                .socioId(socioId)
                .saldoActual(BigDecimal.ZERO)
                .estado(EstadoCuenta.CERRADA)
                .moneda(Moneda.VES)
                .build();
        when(cuentaRepository.buscarPorNumeroCuenta("AHO-2026-000010"))
                .thenReturn(Optional.of(cerrada));

        assertThatThrownBy(() -> useCase.ejecutar(
                "AHO-2026-000010", request, socioId, false, "1.1.1.1", "sess", "req"))
                .isInstanceOf(CuentaNoPermiteOperacionesException.class);

        verifyNoInteractions(contabilidadPort);
    }

    // ─── Propagación de errores del hook ───────────────────────────────────

    @Test
    @DisplayName("si el hook contable lanza, se propaga (rollback)")
    void error_contable_propaga() {
        doThrow(new AsientoContableException("código 2.1.01 no existe"))
                .when(contabilidadPort).registrarRetiro(any(), any());

        assertThatThrownBy(() -> useCase.ejecutar(
                "AHO-2026-000010", request, socioId, false, "1.1.1.1", "sess", "req"))
                .isInstanceOf(AsientoContableException.class)
                .hasMessageContaining("2.1.01");
    }
}
