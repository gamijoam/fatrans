package com.tufondo.compliance.application.service;

import com.tufondo.compliance.domain.model.ConsentimientoLocdoftOperacion;
import com.tufondo.compliance.domain.model.ConsentimientoLocdoftOperacion.TipoOperacion;
import com.tufondo.compliance.domain.repository.ConsentimientoLocdoftRepository;
import com.tufondo.parametros.domain.model.ParametroSistema;
import com.tufondo.parametros.domain.repository.ParametroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests del servicio LOCDOFT (#218 PR-C).
 *
 * <p>Cubre los 3 flujos críticos:
 * (1) monto bajo umbral → no requiere consentimiento, devuelve null
 * (2) monto sobre umbral SIN flag → lanza LocdoftConsentimientoRequeridoException
 * (3) monto sobre umbral CON flag → persiste consentimiento y devuelve registro
 *
 * Plus: fail-open si el parámetro no se puede leer o tiene valor inválido.
 */
@ExtendWith(MockitoExtension.class)
class LocdoftOperacionServiceTest {

    @Mock private ParametroRepository parametroRepository;
    @Mock private ConsentimientoLocdoftRepository consentimientoRepository;

    @InjectMocks
    private LocdoftOperacionService service;

    private UUID socioId;
    private UUID cuentaId;

    @BeforeEach
    void setUp() {
        socioId = UUID.randomUUID();
        cuentaId = UUID.randomUUID();
    }

    private LocdoftOperacionService.DatosOperacion datos(BigDecimal monto, String moneda, boolean confirma, String origen) {
        return new LocdoftOperacionService.DatosOperacion(
                socioId, cuentaId, TipoOperacion.DEPOSITO,
                monto, moneda, confirma, origen,
                "10.0.0.1", "Mozilla/5.0", "sess-1", "req-1"
        );
    }

    private void stubUmbral(String key, String valor) {
        when(parametroRepository.buscarPorKey(key)).thenReturn(Optional.of(
                ParametroSistema.desdeParametros(key, valor,
                        ParametroSistema.TipoParametro.CURRENCY,
                        "test", "compliance", true, Instant.now(), null)));
    }

    @Test
    @DisplayName("Monto bajo umbral VES → devuelve null sin tocar el repo de consentimientos")
    void monto_bajo_umbral_no_requiere_consentimiento() {
        stubUmbral("LOCDOFT_UMBRAL_VES", "10000.00");

        ConsentimientoLocdoftOperacion resultado = service.validarYRegistrar(
                datos(new BigDecimal("500.00"), "VES", false, null));

        assertThat(resultado).isNull();
        verify(consentimientoRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("Monto IGUAL al umbral → no lo supera, devuelve null (boundary)")
    void monto_igual_umbral_no_requiere() {
        stubUmbral("LOCDOFT_UMBRAL_VES", "10000.00");

        ConsentimientoLocdoftOperacion resultado = service.validarYRegistrar(
                datos(new BigDecimal("10000.00"), "VES", false, null));

        assertThat(resultado).isNull();
        verify(consentimientoRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("Monto SOBRE umbral SIN flag → lanza LocdoftConsentimientoRequeridoException con detalles")
    void monto_sobre_umbral_sin_flag_lanza_exception() {
        stubUmbral("LOCDOFT_UMBRAL_VES", "10000.00");

        assertThatThrownBy(() -> service.validarYRegistrar(
                datos(new BigDecimal("15000.00"), "VES", false, null)))
                .isInstanceOf(LocdoftConsentimientoRequeridoException.class)
                .satisfies(t -> {
                    LocdoftConsentimientoRequeridoException e = (LocdoftConsentimientoRequeridoException) t;
                    assertThat(e.getMontoSolicitado()).isEqualByComparingTo("15000.00");
                    assertThat(e.getMoneda()).isEqualTo("VES");
                    assertThat(e.getUmbral()).isEqualByComparingTo("10000.00");
                });

        verify(consentimientoRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("Monto SOBRE umbral CON flag → persiste consentimiento con snapshot del umbral + origenFondos")
    void monto_sobre_umbral_con_flag_persiste() {
        stubUmbral("LOCDOFT_UMBRAL_VES", "10000.00");
        when(consentimientoRepository.guardar(any())).thenAnswer(inv -> {
            ConsentimientoLocdoftOperacion c = inv.getArgument(0);
            // simula que el repo asigna id
            return ConsentimientoLocdoftOperacion.builder()
                    .id(UUID.randomUUID())
                    .socioId(c.getSocioId())
                    .cuentaAhorroId(c.getCuentaAhorroId())
                    .tipoOperacion(c.getTipoOperacion())
                    .monto(c.getMonto())
                    .moneda(c.getMoneda())
                    .umbralAplicado(c.getUmbralAplicado())
                    .aceptaOrigenLicito(c.isAceptaOrigenLicito())
                    .origenFondos(c.getOrigenFondos())
                    .createdAt(Instant.now())
                    .build();
        });

        ConsentimientoLocdoftOperacion resultado = service.validarYRegistrar(
                datos(new BigDecimal("15000.00"), "VES", true, "Venta de vehículo"));

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isNotNull();

        ArgumentCaptor<ConsentimientoLocdoftOperacion> captor =
                ArgumentCaptor.forClass(ConsentimientoLocdoftOperacion.class);
        verify(consentimientoRepository).guardar(captor.capture());
        ConsentimientoLocdoftOperacion guardado = captor.getValue();
        assertThat(guardado.getSocioId()).isEqualTo(socioId);
        assertThat(guardado.getCuentaAhorroId()).isEqualTo(cuentaId);
        assertThat(guardado.getTipoOperacion()).isEqualTo(TipoOperacion.DEPOSITO);
        assertThat(guardado.getMonto()).isEqualByComparingTo("15000.00");
        assertThat(guardado.getUmbralAplicado()).isEqualByComparingTo("10000.00");
        assertThat(guardado.isAceptaOrigenLicito()).isTrue();
        assertThat(guardado.getOrigenFondos()).isEqualTo("Venta de vehículo");
        assertThat(guardado.getMovimientoId()).isNull(); // se asocia después
    }

    @Test
    @DisplayName("Moneda USD usa LOCDOFT_UMBRAL_USD, no el de VES")
    void moneda_usd_lee_parametro_correcto() {
        stubUmbral("LOCDOFT_UMBRAL_USD", "1000.00");

        // 500 USD bajo umbral → null
        assertThat(service.validarYRegistrar(datos(new BigDecimal("500.00"), "USD", false, null))).isNull();

        verify(parametroRepository).buscarPorKey("LOCDOFT_UMBRAL_USD");
        verify(parametroRepository, never()).buscarPorKey("LOCDOFT_UMBRAL_VES");
    }

    @Test
    @DisplayName("Fail-open: si el parámetro no existe, NO bloquea la operación (devuelve null)")
    void parametro_inexistente_fail_open() {
        when(parametroRepository.buscarPorKey("LOCDOFT_UMBRAL_VES")).thenReturn(Optional.empty());

        // Monto grande, sin flag — no lanza porque no hay umbral configurado
        ConsentimientoLocdoftOperacion resultado = service.validarYRegistrar(
                datos(new BigDecimal("9999999.00"), "VES", false, null));

        assertThat(resultado).isNull();
        verify(consentimientoRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("Fail-open: si el parámetro tiene valor no-numérico, NO bloquea (loguea + devuelve null)")
    void parametro_valor_invalido_fail_open() {
        stubUmbral("LOCDOFT_UMBRAL_VES", "esto-no-es-numero");

        ConsentimientoLocdoftOperacion resultado = service.validarYRegistrar(
                datos(new BigDecimal("99999.00"), "VES", false, null));

        assertThat(resultado).isNull();
    }

    @Test
    @DisplayName("Fail-open: si el repo lanza al leer el parámetro, NO bloquea")
    void repo_lanza_fail_open() {
        when(parametroRepository.buscarPorKey(any()))
                .thenThrow(new RuntimeException("DB caída"));

        ConsentimientoLocdoftOperacion resultado = service.validarYRegistrar(
                datos(new BigDecimal("99999.00"), "VES", false, null));

        assertThat(resultado).isNull();
    }

    @Test
    @DisplayName("asociarConMovimiento: invoca al repo cuando ambos ids son no-null")
    void asociar_con_movimiento_ok() {
        UUID consentId = UUID.randomUUID();
        UUID movId = UUID.randomUUID();

        service.asociarConMovimiento(consentId, movId);

        verify(consentimientoRepository).asociarConMovimiento(consentId, movId);
    }

    @Test
    @DisplayName("asociarConMovimiento: con ids null hace no-op (resiliente)")
    void asociar_con_movimiento_null_noop() {
        service.asociarConMovimiento(null, UUID.randomUUID());
        service.asociarConMovimiento(UUID.randomUUID(), null);
        service.asociarConMovimiento(null, null);

        verify(consentimientoRepository, never()).asociarConMovimiento(any(), any());
    }

    @Test
    @DisplayName("asociarConMovimiento: si el repo lanza, NO propaga (la operación principal no debe abortar)")
    void asociar_con_movimiento_falla_no_propaga() {
        doThrow(new RuntimeException("DB caída"))
                .when(consentimientoRepository).asociarConMovimiento(any(), any());

        // NO debe lanzar
        service.asociarConMovimiento(UUID.randomUUID(), UUID.randomUUID());
    }
}
