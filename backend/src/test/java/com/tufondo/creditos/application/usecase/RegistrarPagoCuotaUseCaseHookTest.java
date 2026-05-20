package com.tufondo.creditos.application.usecase;

import com.tufondo.contabilidad.application.exception.AsientoContableException;
import com.tufondo.creditos.application.mapper.CreditosDTOMapper;
import com.tufondo.creditos.application.port.output.CreditosContabilidadPort;
import com.tufondo.creditos.domain.exception.CreditoNoEncontradoException;
import com.tufondo.creditos.domain.exception.CuotaNoEncontradaException;
import com.tufondo.creditos.domain.exception.CuotaYaPagadaException;
import com.tufondo.creditos.domain.exception.MontoInsuficienteException;
import com.tufondo.creditos.domain.exception.PagoDuplicadoException;
import com.tufondo.creditos.domain.model.Amortizacion;
import com.tufondo.creditos.domain.model.PlanAmortizacion;
import com.tufondo.creditos.domain.model.SolicitudCredito;
import com.tufondo.creditos.domain.model.enums.EstadoAmortizacion;
import com.tufondo.creditos.domain.repository.AmortizacionRepository;
import com.tufondo.creditos.domain.repository.CuentaGarantiaRepository;
import com.tufondo.creditos.domain.repository.PlanAmortizacionRepository;
import com.tufondo.creditos.domain.repository.SolicitudCreditoRepository;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests del hook contable inyectado en {@link RegistrarPagoCuotaUseCase}
 * (sub-issue #268).
 *
 * <p>Foco: validar que cada pago confirmado invoque al hook con los datos
 * correctos, y que si una validación previa rechaza el pago, el hook NO se
 * llame. También verifica propagación de errores contables.</p>
 */
@ExtendWith(MockitoExtension.class)
class RegistrarPagoCuotaUseCaseHookTest {

    @Mock private AmortizacionRepository amortizacionRepository;
    @Mock private PlanAmortizacionRepository planRepository;
    @Mock private SolicitudCreditoRepository solicitudRepository;
    @Mock private CuentaGarantiaRepository cuentaGarantiaRepository;
    @Mock private CreditosDTOMapper mapper;
    @Mock private CreditosContabilidadPort contabilidadPort;

    @InjectMocks private RegistrarPagoCuotaUseCase useCase;

    private UUID cuotaId;
    private Amortizacion cuota;
    private PlanAmortizacion plan;
    private SolicitudCredito solicitud;

    @BeforeEach
    void setUp() {
        cuotaId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        UUID solicitudId = UUID.randomUUID();

        plan = PlanAmortizacion.builder()
                .id(planId)
                .solicitudId(solicitudId)
                .saldoPendiente(new BigDecimal("9500.00"))
                .totalPagado(BigDecimal.ZERO)  // requerido por registrarPago(monto)
                .build();

        cuota = Amortizacion.builder()
                .id(cuotaId)
                .planId(planId)
                .plan(plan)
                .numeroCuota(3)
                .capital(new BigDecimal("400.00"))
                .interes(new BigDecimal("100.00"))
                .interesMora(BigDecimal.ZERO)
                .montoCuota(new BigDecimal("500.00"))
                .estado(EstadoAmortizacion.PENDIENTE)
                .build();

        solicitud = SolicitudCredito.builder()
                .id(solicitudId)
                .numeroSolicitud("SOL-CRED-2026-TEST")
                .socioId(UUID.randomUUID())
                .montoSolicitado(new BigDecimal("10000.00"))
                .build();

        // Mocks por defecto — happy path. Lenient: validaciones tempranas en
        // algunos tests cortan antes y no consumen estos stubs.
        lenient().when(amortizacionRepository.existePorReferenciaPago(any())).thenReturn(false);
        lenient().when(amortizacionRepository.buscarPorIdWithLock(cuotaId)).thenReturn(Optional.of(cuota));
        lenient().when(planRepository.buscarPorId(plan.getId())).thenReturn(Optional.of(plan));
        lenient().when(solicitudRepository.buscarPorId(plan.getSolicitudId()))
                .thenReturn(Optional.of(solicitud));
    }

    // ─── Happy path ────────────────────────────────────────────────────────

    @Test
    @DisplayName("pago válido invoca al hook contable con solicitud, cuota y monto")
    void pago_invoca_hook_contable() {
        useCase.ejecutar(cuotaId, new BigDecimal("500.00"), "REF-001", "WEB");

        verify(contabilidadPort).registrarPagoCuota(
                eq(solicitud), eq(cuota), eq(new BigDecimal("500.00")), eq("REF-001"));
    }

    // ─── Validaciones que cortan ANTES del hook ────────────────────────────

    @Test
    @DisplayName("referencia duplicada → NO se llama al hook")
    void referencia_duplicada_no_llama_hook() {
        when(amortizacionRepository.existePorReferenciaPago("REF-DUP")).thenReturn(true);

        assertThatThrownBy(() -> useCase.ejecutar(
                cuotaId, new BigDecimal("500.00"), "REF-DUP", "WEB"))
                .isInstanceOf(PagoDuplicadoException.class);

        verifyNoInteractions(contabilidadPort);
    }

    @Test
    @DisplayName("cuota inexistente → NO se llama al hook")
    void cuota_inexistente_no_llama_hook() {
        UUID otraCuota = UUID.randomUUID();
        when(amortizacionRepository.buscarPorIdWithLock(otraCuota)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.ejecutar(
                otraCuota, new BigDecimal("500.00"), "REF-X", "WEB"))
                .isInstanceOf(CuotaNoEncontradaException.class);

        verifyNoInteractions(contabilidadPort);
    }

    @Test
    @DisplayName("cuota ya PAGADA → NO se llama al hook")
    void cuota_ya_pagada_no_llama_hook() {
        cuota.setEstado(EstadoAmortizacion.PAGADA);

        assertThatThrownBy(() -> useCase.ejecutar(
                cuotaId, new BigDecimal("500.00"), "REF-X", "WEB"))
                .isInstanceOf(CuotaYaPagadaException.class);

        verifyNoInteractions(contabilidadPort);
    }

    @Test
    @DisplayName("monto insuficiente → NO se llama al hook")
    void monto_insuficiente_no_llama_hook() {
        assertThatThrownBy(() -> useCase.ejecutar(
                cuotaId, new BigDecimal("100.00"), "REF-X", "WEB"))
                .isInstanceOf(MontoInsuficienteException.class);

        verifyNoInteractions(contabilidadPort);
    }

    // ─── Propagación de errores del hook ───────────────────────────────────

    @Test
    @DisplayName("si el hook contable falla, propaga y hace rollback")
    void error_contable_propaga() {
        doThrow(new AsientoContableException("cuenta 1.1.03 no encontrada"))
                .when(contabilidadPort).registrarPagoCuota(any(), any(), any(), any());

        assertThatThrownBy(() -> useCase.ejecutar(
                cuotaId, new BigDecimal("500.00"), "REF-OK", "WEB"))
                .isInstanceOf(AsientoContableException.class)
                .hasMessageContaining("1.1.03");
    }
}
