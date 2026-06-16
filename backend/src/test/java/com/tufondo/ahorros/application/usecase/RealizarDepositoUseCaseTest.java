package com.tufondo.ahorros.application.usecase;

import com.tufondo.ahorros.application.dto.DepositoRequest;
import com.tufondo.ahorros.application.dto.MovimientoResponse;
import com.tufondo.ahorros.application.mapper.AhorrosDTOMapper;
import com.tufondo.ahorros.application.port.output.AhorrosContabilidadPort;
import com.tufondo.ahorros.domain.exception.AccesoCuentaAjenaException;
import com.tufondo.ahorros.domain.exception.CuentaAhorroNoEncontradaException;
import com.tufondo.ahorros.domain.exception.CuentaNoPermiteOperacionesException;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests del use case de depósito incluyendo el hook contable (#267).
 *
 * <p>Foco: validar que cada depósito exitoso invoque al hook contable y
 * que cuando el hook lanza, el use case propaga la excepción (para que
 * el {@code @Transactional} haga rollback de saldo + movimiento + asiento).</p>
 */
@ExtendWith(MockitoExtension.class)
class RealizarDepositoUseCaseTest {

    @Mock private CuentaAhorroRepository cuentaRepository;
    @Mock private MovimientoRepository movimientoRepository;
    @Mock private AhorrosDTOMapper mapper;
    @Mock private LocdoftOperacionService locdoftService;
    @Mock private AhorrosContabilidadPort contabilidadPort;

    @InjectMocks private RealizarDepositoUseCase useCase;

    private UUID socioId;
    private CuentaAhorro cuentaActiva;
    private DepositoRequest request;

    @BeforeEach
    void setUp() {
        socioId = UUID.randomUUID();
        cuentaActiva = CuentaAhorro.builder()
                .id(UUID.randomUUID())
                .numeroCuenta("AHO-2026-000001")
                .socioId(socioId)
                .saldoActual(new BigDecimal("500.00"))
                .saldoRetenido(BigDecimal.ZERO)
                .estado(EstadoCuenta.ACTIVA)
                .moneda(Moneda.VES)
                .build();

        request = new DepositoRequest(
                new BigDecimal("100.00"),  // monto
                "Depósito de prueba",
                "REF-001",
                CanalOrigen.WEB,
                null,  // confirmaOrigenLicito
                null   // origenFondos
        );

        // Mocks por defecto — happy path. Lenient porque algunos tests cortan
        // antes (IDOR, cuenta cerrada, etc) y no usan estos stubs.
        lenient().when(cuentaRepository.buscarPorNumeroCuenta("AHO-2026-000001"))
                .thenReturn(Optional.of(cuentaActiva));
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
    @DisplayName("depósito válido invoca al hook contable con cuenta y movimiento")
    void deposito_invoca_hook_contable() {
        useCase.ejecutar("AHO-2026-000001", request, socioId, false, "1.1.1.1", "sess", "req");

        verify(contabilidadPort).registrarDeposito(eq(cuentaActiva), any(Movimiento.class));
        verify(movimientoRepository).guardar(any(Movimiento.class));
        verify(cuentaRepository).guardar(cuentaActiva);
    }

    @Test
    @DisplayName("saldo se incrementa con el monto del depósito")
    void deposito_incrementa_saldo() {
        useCase.ejecutar("AHO-2026-000001", request, socioId, false, "1.1.1.1", "sess", "req");

        assertThat(cuentaActiva.getSaldoActual()).isEqualByComparingTo("600.00");
    }

    // ─── Propagación de errores del hook contable ──────────────────────────

    @Test
    @DisplayName("si el hook contable falla, la excepción se propaga (rollback)")
    void error_contable_propaga_para_rollback() {
        doThrow(new AsientoContableException("plan de cuentas no inicializado"))
                .when(contabilidadPort).registrarDeposito(any(), any());

        assertThatThrownBy(() -> useCase.ejecutar(
                "AHO-2026-000001", request, socioId, false, "1.1.1.1", "sess", "req"))
                .isInstanceOf(AsientoContableException.class)
                .hasMessageContaining("plan de cuentas");
    }

    @Test
    @DisplayName("si la cuenta no existe, NO se llama al hook contable")
    void cuenta_inexistente_no_llama_hook() {
        when(cuentaRepository.buscarPorNumeroCuenta("AHO-INVALID"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.ejecutar(
                "AHO-INVALID", request, socioId, false, "1.1.1.1", "sess", "req"))
                .isInstanceOf(CuentaAhorroNoEncontradaException.class);

        verifyNoInteractions(contabilidadPort);
    }

    @Test
    @DisplayName("IDOR: socio intentando depositar en cuenta ajena NO llega al hook")
    void idor_no_llega_a_contabilidad() {
        UUID otroSocio = UUID.randomUUID();

        assertThatThrownBy(() -> useCase.ejecutar(
                "AHO-2026-000001", request, otroSocio, false, "1.1.1.1", "sess", "req"))
                .isInstanceOf(AccesoCuentaAjenaException.class);

        verifyNoInteractions(contabilidadPort);
    }

    @Test
    @DisplayName("cuenta CERRADA: lanza antes de contabilidad")
    void cuenta_cerrada_no_llega_a_contabilidad() {
        CuentaAhorro cerrada = CuentaAhorro.builder()
                .id(cuentaActiva.getId())
                .numeroCuenta("AHO-2026-000001")
                .socioId(socioId)
                .saldoActual(BigDecimal.ZERO)
                .estado(EstadoCuenta.CERRADA)
                .moneda(Moneda.VES)
                .build();
        when(cuentaRepository.buscarPorNumeroCuenta("AHO-2026-000001"))
                .thenReturn(Optional.of(cerrada));

        assertThatThrownBy(() -> useCase.ejecutar(
                "AHO-2026-000001", request, socioId, false, "1.1.1.1", "sess", "req"))
                .isInstanceOf(CuentaNoPermiteOperacionesException.class);

        verifyNoInteractions(contabilidadPort);
    }
}
