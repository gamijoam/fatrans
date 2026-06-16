package com.tufondo.creditos.infrastructure.contabilidad;

import com.tufondo.contabilidad.application.dto.RegistrarAsientoCommand;
import com.tufondo.contabilidad.application.exception.AsientoContableException;
import com.tufondo.contabilidad.application.usecase.AsientoContableService;
import com.tufondo.contabilidad.domain.model.enums.OrigenAsiento;
import com.tufondo.creditos.domain.model.Amortizacion;
import com.tufondo.creditos.domain.model.SolicitudCredito;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Tests unitarios del adapter que materializa operaciones de Créditos como
 * asientos contables de partida doble (sub-issue #268).
 *
 * <p>Cubre las variantes:</p>
 * <ul>
 *   <li>Desembolso con/sin comisión apertura</li>
 *   <li>Pago de cuota con/sin mora</li>
 *   <li>Validación de cuadre (bruto = neto + comisión)</li>
 *   <li>Validación de cuadre (cobrado = capital + interés + mora)</li>
 *   <li>Propagación de excepciones</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class CreditosContabilidadAdapterTest {

    @Mock private AsientoContableService asientoContableService;
    @InjectMocks private CreditosContabilidadAdapter adapter;

    private SolicitudCredito solicitud;
    private Amortizacion cuotaNormal;
    private Amortizacion cuotaConMora;

    @BeforeEach
    void setUp() {
        solicitud = SolicitudCredito.builder()
                .id(UUID.randomUUID())
                .numeroSolicitud("SOL-CRED-2026-000123")
                .socioId(UUID.randomUUID())
                .montoSolicitado(new BigDecimal("10000.00"))
                .cuentaDestino("0134-0001-23-0000001234")
                .build();

        // Cuota sin mora — total 500: 400 capital + 100 interés
        cuotaNormal = Amortizacion.builder()
                .id(UUID.randomUUID())
                .numeroCuota(3)
                .capital(new BigDecimal("400.00"))
                .interes(new BigDecimal("100.00"))
                .interesMora(BigDecimal.ZERO)
                .montoCuota(new BigDecimal("500.00"))
                .build();

        // Cuota con mora — total 530: 400 capital + 100 interés + 30 mora
        cuotaConMora = Amortizacion.builder()
                .id(UUID.randomUUID())
                .numeroCuota(7)
                .capital(new BigDecimal("400.00"))
                .interes(new BigDecimal("100.00"))
                .interesMora(new BigDecimal("30.00"))
                .montoCuota(new BigDecimal("500.00"))
                .build();
    }

    // ═══ Desembolso ═══════════════════════════════════════════════════════

    @Nested
    @DisplayName("Desembolso — DEBE cartera / HABER bancos + comisión opcional")
    class Desembolso {

        @Test
        @DisplayName("Sin comisión: 2 partidas (DEBE 1.3.01 / HABER 1.1.03)")
        void desembolso_sin_comision() {
            adapter.registrarDesembolso(solicitud, new BigDecimal("10000.00"), BigDecimal.ZERO);

            RegistrarAsientoCommand cmd = capturarComando();
            assertThat(cmd.origen()).isEqualTo(OrigenAsiento.CREDITO_DESEMBOLSO);
            assertThat(cmd.referenciaExterna()).isEqualTo("SOL-CRED-2026-000123");
            assertThat(cmd.partidas()).hasSize(2);

            // DEBE Cartera
            var debe = cmd.partidas().get(0);
            assertThat(debe.codigoCuenta()).isEqualTo("1.3.01");
            assertThat(debe.debe()).isEqualByComparingTo("10000.00");
            assertThat(debe.haber()).isNull();

            // HABER Bancos Bs
            var haber = cmd.partidas().get(1);
            assertThat(haber.codigoCuenta()).isEqualTo("1.1.03");
            assertThat(haber.haber()).isEqualByComparingTo("10000.00");
        }

        @Test
        @DisplayName("Con comisión: 3 partidas (DEBE 1.3.01 bruto / HABER 1.1.03 neto / HABER 4.1.02 comisión)")
        void desembolso_con_comision() {
            // bruto 10000 = neto 9500 + comisión 500
            adapter.registrarDesembolso(solicitud, new BigDecimal("9500.00"), new BigDecimal("500.00"));

            RegistrarAsientoCommand cmd = capturarComando();
            assertThat(cmd.partidas()).hasSize(3);

            assertThat(cmd.partidas().get(0).codigoCuenta()).isEqualTo("1.3.01");
            assertThat(cmd.partidas().get(0).debe()).isEqualByComparingTo("10000.00");

            assertThat(cmd.partidas().get(1).codigoCuenta()).isEqualTo("1.1.03");
            assertThat(cmd.partidas().get(1).haber()).isEqualByComparingTo("9500.00");

            assertThat(cmd.partidas().get(2).codigoCuenta()).isEqualTo("4.1.02");
            assertThat(cmd.partidas().get(2).haber()).isEqualByComparingTo("500.00");
        }

        @Test
        @DisplayName("Comisión null tratada como cero (sin partida 4.1.02)")
        void comision_null_no_genera_partida() {
            adapter.registrarDesembolso(solicitud, new BigDecimal("10000.00"), null);

            RegistrarAsientoCommand cmd = capturarComando();
            assertThat(cmd.partidas()).hasSize(2);
            assertThat(cmd.partidas()).extracting("codigoCuenta")
                    .doesNotContain("4.1.02");
        }

        @Test
        @DisplayName("Bruto ≠ neto + comisión → rechazo CON mensaje claro antes de llamar a contabilidad")
        void desembolso_desbalanceado_se_rechaza_localmente() {
            // bruto 10000, pero neto 9000 + comisión 500 = 9500 (faltan 500)
            assertThatThrownBy(() -> adapter.registrarDesembolso(
                    solicitud, new BigDecimal("9000.00"), new BigDecimal("500.00")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("desbalanceado")
                    .hasMessageContaining("SOL-CRED-2026-000123");

            // Y NO se llamó a AsientoContableService — el error fue local
            verifyNoInteractions(asientoContableService);
        }

        @Test
        @DisplayName("Glosa incluye número de solicitud y monto bruto")
        void glosa_incluye_metadata() {
            adapter.registrarDesembolso(solicitud, new BigDecimal("10000.00"), BigDecimal.ZERO);

            RegistrarAsientoCommand cmd = capturarComando();
            assertThat(cmd.glosa())
                    .contains("SOL-CRED-2026-000123")
                    .contains("10000");
        }

        @Test
        @DisplayName("Glosa del HABER 1.1.03 incluye cuenta destino truncada si es larga")
        void glosa_haber_incluye_cuenta_destino() {
            adapter.registrarDesembolso(solicitud, new BigDecimal("10000.00"), BigDecimal.ZERO);

            RegistrarAsientoCommand cmd = capturarComando();
            var haber = cmd.partidas().get(1);
            assertThat(haber.glosa()).contains("0134-0001-23-0000001234");
        }
    }

    // ═══ Pago de cuota ════════════════════════════════════════════════════

    @Nested
    @DisplayName("Pago cuota — DEBE bancos / HABER cartera+intereses(+mora)")
    class PagoCuota {

        @Test
        @DisplayName("Sin mora: 3 partidas (DEBE 1.1.03 / HABER 1.3.01 capital / HABER 4.1.01 interés)")
        void pago_sin_mora_3_partidas() {
            adapter.registrarPagoCuota(solicitud, cuotaNormal,
                    new BigDecimal("500.00"), "REF-PAGO-001");

            RegistrarAsientoCommand cmd = capturarComando();
            assertThat(cmd.origen()).isEqualTo(OrigenAsiento.CREDITO_COBRO);
            assertThat(cmd.referenciaExterna()).isEqualTo("REF-PAGO-001");
            assertThat(cmd.partidas()).hasSize(3);

            // DEBE Bancos
            assertThat(cmd.partidas().get(0).codigoCuenta()).isEqualTo("1.1.03");
            assertThat(cmd.partidas().get(0).debe()).isEqualByComparingTo("500.00");

            // HABER Cartera (capital)
            assertThat(cmd.partidas().get(1).codigoCuenta()).isEqualTo("1.3.01");
            assertThat(cmd.partidas().get(1).haber()).isEqualByComparingTo("400.00");

            // HABER Intereses
            assertThat(cmd.partidas().get(2).codigoCuenta()).isEqualTo("4.1.01");
            assertThat(cmd.partidas().get(2).haber()).isEqualByComparingTo("100.00");

            // NO debe haber partida de mora
            assertThat(cmd.partidas()).extracting("codigoCuenta")
                    .doesNotContain("4.1.03");
        }

        @Test
        @DisplayName("Con mora > 0: 4 partidas (la 4ta HABER 4.1.03 mora)")
        void pago_con_mora_4_partidas() {
            adapter.registrarPagoCuota(solicitud, cuotaConMora,
                    new BigDecimal("530.00"), "REF-PAGO-MORA");

            RegistrarAsientoCommand cmd = capturarComando();
            assertThat(cmd.partidas()).hasSize(4);

            assertThat(cmd.partidas().get(3).codigoCuenta()).isEqualTo("4.1.03");
            assertThat(cmd.partidas().get(3).haber()).isEqualByComparingTo("30.00");
        }

        @Test
        @DisplayName("Mora exactamente 0 NO genera partida 4.1.03")
        void mora_cero_no_genera_partida() {
            adapter.registrarPagoCuota(solicitud, cuotaNormal,
                    new BigDecimal("500.00"), "REF-001");

            RegistrarAsientoCommand cmd = capturarComando();
            assertThat(cmd.partidas()).extracting("codigoCuenta")
                    .doesNotContain("4.1.03");
        }

        @Test
        @DisplayName("Cobrado ≠ capital + interés + mora → rechazo (probable seguros/comisiones intra-cuota)")
        void pago_desbalanceado_se_rechaza() {
            // cuotaNormal espera total 500, pero se "cobró" 600 → 100 sin clasificar
            assertThatThrownBy(() -> adapter.registrarPagoCuota(
                    solicitud, cuotaNormal, new BigDecimal("600.00"), "REF-X"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("desbalanceado")
                    .hasMessageContaining("seguros/comisiones");

            verifyNoInteractions(asientoContableService);
        }

        @Test
        @DisplayName("Referencia null/vacía → genera referencia fallback 'CUOTA-{id}'")
        void referencia_null_usa_fallback() {
            adapter.registrarPagoCuota(solicitud, cuotaNormal, new BigDecimal("500.00"), null);

            RegistrarAsientoCommand cmd = capturarComando();
            assertThat(cmd.referenciaExterna()).startsWith("CUOTA-");
        }
    }

    // ═══ Propagación de errores ═══════════════════════════════════════════

    @Test
    @DisplayName("excepción de AsientoContableService se propaga (no se traga)")
    void error_contable_se_propaga_en_desembolso() {
        doThrow(new AsientoContableException("plan de cuentas no inicializado"))
                .when(asientoContableService).registrar(org.mockito.ArgumentMatchers.any());

        assertThatThrownBy(() -> adapter.registrarDesembolso(
                solicitud, new BigDecimal("10000.00"), BigDecimal.ZERO))
                .isInstanceOf(AsientoContableException.class)
                .hasMessageContaining("plan de cuentas");
    }

    @Test
    @DisplayName("excepción de AsientoContableService se propaga en pago cuota también")
    void error_contable_se_propaga_en_pago_cuota() {
        doThrow(new AsientoContableException("cuenta 4.1.01 no encontrada"))
                .when(asientoContableService).registrar(org.mockito.ArgumentMatchers.any());

        assertThatThrownBy(() -> adapter.registrarPagoCuota(
                solicitud, cuotaNormal, new BigDecimal("500.00"), "REF"))
                .isInstanceOf(AsientoContableException.class)
                .hasMessageContaining("4.1.01");
    }

    // ─── Helper ────────────────────────────────────────────────────────────

    private RegistrarAsientoCommand capturarComando() {
        ArgumentCaptor<RegistrarAsientoCommand> captor =
                ArgumentCaptor.forClass(RegistrarAsientoCommand.class);
        verify(asientoContableService).registrar(captor.capture());
        return captor.getValue();
    }
}
