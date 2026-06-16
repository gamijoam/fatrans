package com.tufondo.creditos.application.usecase;

import com.tufondo.contabilidad.application.exception.AsientoContableException;
import com.tufondo.creditos.application.dto.DesembolsaRequest;
import com.tufondo.creditos.application.mapper.CreditosDTOMapper;
import com.tufondo.creditos.application.port.output.CreditosContabilidadPort;
import com.tufondo.creditos.domain.exception.CreditoNoEncontradoException;
import com.tufondo.creditos.domain.exception.EstadoCreditoInvalidoException;
import com.tufondo.creditos.domain.model.SolicitudCredito;
import com.tufondo.creditos.domain.model.TipoCredito;
import com.tufondo.creditos.domain.model.enums.EstadoSolicitud;
import com.tufondo.creditos.domain.repository.PlanAmortizacionRepository;
import com.tufondo.creditos.domain.repository.SolicitudCreditoRepository;
import com.tufondo.creditos.domain.repository.TipoCreditoRepository;
import com.tufondo.notificaciones.application.service.NotificacionPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
 * Tests del hook contable inyectado en {@link DesembolsaCreditoUseCase}
 * (sub-issue #268).
 *
 * <p>Foco: validar que el hook reciba montoNeto y comisionApertura calculados
 * correctamente según el TipoCredito, y que NO se invoque si una validación
 * previa falla (solicitud no aprobada, inexistente).</p>
 */
@ExtendWith(MockitoExtension.class)
class DesembolsaCreditoUseCaseHookTest {

    @Mock private SolicitudCreditoRepository solicitudRepository;
    @Mock private TipoCreditoRepository tipoCreditoRepository;
    @Mock private PlanAmortizacionRepository planRepository;
    @Mock private CreditosDTOMapper mapper;
    @Mock private NotificacionPublisher notificacionPublisher;
    @Mock private CreditosContabilidadPort contabilidadPort;

    @InjectMocks private DesembolsaCreditoUseCase useCase;

    private SolicitudCredito solicitud;
    private TipoCredito tipoCredito;
    private DesembolsaRequest request;
    private static final String NUMERO = "SOL-CRED-2026-DESEMB-001";

    @BeforeEach
    void setUp() {
        Long tipoId = 1L;
        solicitud = SolicitudCredito.builder()
                .id(UUID.randomUUID())
                .numeroSolicitud(NUMERO)
                .socioId(UUID.randomUUID())
                .tipoCreditoId(tipoId)
                .montoSolicitado(new BigDecimal("10000.00"))
                .plazoMeses(12)
                .tasaInteresAplicada(new BigDecimal("0.0250"))  // 2.5% mensual
                .estado(EstadoSolicitud.APROBADA)
                .build();

        tipoCredito = TipoCredito.builder()
                .id(tipoId)
                .comisionApertura(new BigDecimal("0.05"))  // 5% del monto solicitado = 500
                .build();

        request = new DesembolsaRequest();
        request.setReferenciaDesembolso("TRANSF-BANCESCO-001");

        // Mocks por defecto — happy path
        lenient().when(solicitudRepository.buscarPorNumeroSolicitud(NUMERO))
                .thenReturn(Optional.of(solicitud));
        lenient().when(tipoCreditoRepository.buscarPorId(tipoId))
                .thenReturn(Optional.of(tipoCredito));
    }

    // ─── Happy path ────────────────────────────────────────────────────────

    @Test
    @DisplayName("desembolso válido invoca al hook con montoNeto y comisión calculados")
    void desembolso_invoca_hook_contable_con_calculo_correcto() {
        useCase.ejecutar(NUMERO, request, "1.1.1.1");

        // monto bruto 10000, comisión 5% = 500, neto = 9500
        ArgumentCaptor<BigDecimal> netoCap = ArgumentCaptor.forClass(BigDecimal.class);
        ArgumentCaptor<BigDecimal> comisionCap = ArgumentCaptor.forClass(BigDecimal.class);
        verify(contabilidadPort).registrarDesembolso(
                eq(solicitud), netoCap.capture(), comisionCap.capture());

        assertThat(netoCap.getValue()).isEqualByComparingTo("9500.00");
        assertThat(comisionCap.getValue()).isEqualByComparingTo("500.00");
    }

    @Test
    @DisplayName("comisión apertura cero (TipoCredito sin comisión) → neto = bruto")
    void desembolso_sin_comision() {
        tipoCredito.setComisionApertura(BigDecimal.ZERO);

        useCase.ejecutar(NUMERO, request, "1.1.1.1");

        ArgumentCaptor<BigDecimal> netoCap = ArgumentCaptor.forClass(BigDecimal.class);
        ArgumentCaptor<BigDecimal> comisionCap = ArgumentCaptor.forClass(BigDecimal.class);
        verify(contabilidadPort).registrarDesembolso(
                eq(solicitud), netoCap.capture(), comisionCap.capture());

        assertThat(netoCap.getValue()).isEqualByComparingTo("10000.00");
        assertThat(comisionCap.getValue()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("comisión override en request tiene precedencia sobre TipoCredito")
    void desembolso_con_comision_override() {
        request.setComisionAperturaAplicada(new BigDecimal("200.00"));

        useCase.ejecutar(NUMERO, request, "1.1.1.1");

        ArgumentCaptor<BigDecimal> comisionCap = ArgumentCaptor.forClass(BigDecimal.class);
        verify(contabilidadPort).registrarDesembolso(
                eq(solicitud), any(), comisionCap.capture());

        assertThat(comisionCap.getValue()).isEqualByComparingTo("200.00");
    }

    @Test
    @DisplayName("se notifica al socio TRAS el hook contable (no antes)")
    void notificacion_va_despues_del_hook() {
        useCase.ejecutar(NUMERO, request, "1.1.1.1");

        // Verificación de orden: el hook DEBE ejecutarse antes que la notificación
        // para que un rollback de contabilidad evite mensaje al socio que no corresponde.
        var inOrder = inOrder(contabilidadPort, notificacionPublisher);
        inOrder.verify(contabilidadPort).registrarDesembolso(any(), any(), any());
        inOrder.verify(notificacionPublisher)
                .notificarSocioCreditoDesembolsado(any(), any(), any(), any());
    }

    // ─── Validaciones que cortan ANTES del hook ────────────────────────────

    @Test
    @DisplayName("solicitud inexistente → NO se llama al hook")
    void solicitud_inexistente_no_llama_hook() {
        when(solicitudRepository.buscarPorNumeroSolicitud("SOL-INVALID"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.ejecutar("SOL-INVALID", request, "1.1.1.1"))
                .isInstanceOf(CreditoNoEncontradoException.class);

        verifyNoInteractions(contabilidadPort);
        verifyNoInteractions(notificacionPublisher);
    }

    @Test
    @DisplayName("solicitud en estado no APROBADA → NO se llama al hook")
    void solicitud_no_aprobada_no_llama_hook() {
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);

        assertThatThrownBy(() -> useCase.ejecutar(NUMERO, request, "1.1.1.1"))
                .isInstanceOf(EstadoCreditoInvalidoException.class);

        verifyNoInteractions(contabilidadPort);
    }

    // ─── Propagación de errores del hook ───────────────────────────────────

    @Test
    @DisplayName("error contable propaga (rollback) y NO se envía notificación")
    void error_contable_propaga_sin_notificar() {
        doThrow(new AsientoContableException("cuenta 1.3.01 no encontrada"))
                .when(contabilidadPort).registrarDesembolso(any(), any(), any());

        assertThatThrownBy(() -> useCase.ejecutar(NUMERO, request, "1.1.1.1"))
                .isInstanceOf(AsientoContableException.class)
                .hasMessageContaining("1.3.01");

        // Si la contabilidad falla, NO debe enviar notificación al socio
        // (el desembolso lógicamente no ocurrió).
        verifyNoInteractions(notificacionPublisher);
    }
}
