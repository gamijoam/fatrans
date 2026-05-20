package com.tufondo.ahorros.infrastructure.contabilidad;

import com.tufondo.ahorros.domain.model.CuentaAhorro;
import com.tufondo.ahorros.domain.model.Movimiento;
import com.tufondo.ahorros.domain.model.enums.Moneda;
import com.tufondo.contabilidad.application.dto.RegistrarAsientoCommand;
import com.tufondo.contabilidad.application.exception.AsientoContableException;
import com.tufondo.contabilidad.application.usecase.AsientoContableService;
import com.tufondo.contabilidad.domain.model.enums.OrigenAsiento;
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

/**
 * Tests del adapter que materializa operaciones de Ahorros como asientos
 * contables de partida doble (sub-issue #267).
 *
 * <p>Cubre las 4 variantes (depósito/retiro × Bs/USD) más casos borde:
 * moneda null (fallback Bs), propagación de excepciones, monto exacto, etc.</p>
 */
@ExtendWith(MockitoExtension.class)
class AhorrosContabilidadAdapterTest {

    @Mock private AsientoContableService asientoContableService;

    @InjectMocks private AhorrosContabilidadAdapter adapter;

    private CuentaAhorro cuentaBs;
    private CuentaAhorro cuentaUsd;
    private CuentaAhorro cuentaSinMoneda;
    private Movimiento depositoBs;
    private Movimiento retiroBs;
    private Movimiento depositoUsd;

    @BeforeEach
    void setUp() {
        cuentaBs = CuentaAhorro.builder()
                .id(UUID.randomUUID())
                .numeroCuenta("AHO-2026-000001")
                .socioId(UUID.randomUUID())
                .moneda(Moneda.VES)
                .build();

        cuentaUsd = CuentaAhorro.builder()
                .id(UUID.randomUUID())
                .numeroCuenta("AHO-2026-000002")
                .socioId(UUID.randomUUID())
                .moneda(Moneda.USD)
                .build();

        cuentaSinMoneda = CuentaAhorro.builder()
                .id(UUID.randomUUID())
                .numeroCuenta("AHO-LEGACY-000001")
                .socioId(UUID.randomUUID())
                .moneda(null)  // simula cuenta antigua sin moneda asignada
                .build();

        depositoBs = Movimiento.builder()
                .id(UUID.randomUUID())
                .numeroOperacion("MOV-2026-000001")
                .monto(new BigDecimal("1500.50"))
                .build();

        retiroBs = Movimiento.builder()
                .id(UUID.randomUUID())
                .numeroOperacion("MOV-2026-000002")
                .monto(new BigDecimal("250.00"))
                .build();

        depositoUsd = Movimiento.builder()
                .id(UUID.randomUUID())
                .numeroOperacion("MOV-2026-000003")
                .monto(new BigDecimal("100.00"))
                .build();
    }

    // ─── Depósito ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Depósito → asiento DEBE activo / HABER pasivo")
    class Deposito {

        @Test
        @DisplayName("Bs: DEBE 1.1.01 Caja Principal / HABER 2.1.01 Cuentas de Ahorro Bs")
        void deposito_bs_usa_caja_y_depositos_bs() {
            adapter.registrarDeposito(cuentaBs, depositoBs);

            RegistrarAsientoCommand cmd = capturarComando();
            assertThat(cmd.origen()).isEqualTo(OrigenAsiento.AHORRO_DEPOSITO);
            assertThat(cmd.referenciaExterna()).isEqualTo("MOV-2026-000001");
            assertThat(cmd.partidas()).hasSize(2);

            var debe = cmd.partidas().get(0);
            assertThat(debe.codigoCuenta()).isEqualTo("1.1.01");
            assertThat(debe.debe()).isEqualByComparingTo("1500.50");
            assertThat(debe.haber()).isNull();

            var haber = cmd.partidas().get(1);
            assertThat(haber.codigoCuenta()).isEqualTo("2.1.01");
            assertThat(haber.haber()).isEqualByComparingTo("1500.50");
            assertThat(haber.debe()).isNull();
        }

        @Test
        @DisplayName("USD: DEBE 1.1.05 Bancos USD / HABER 2.1.02 Cuentas de Ahorro USD")
        void deposito_usd_usa_bancos_usd_y_depositos_usd() {
            adapter.registrarDeposito(cuentaUsd, depositoUsd);

            RegistrarAsientoCommand cmd = capturarComando();
            assertThat(cmd.partidas().get(0).codigoCuenta()).isEqualTo("1.1.05");
            assertThat(cmd.partidas().get(1).codigoCuenta()).isEqualTo("2.1.02");
            assertThat(cmd.partidas().get(0).debe()).isEqualByComparingTo("100.00");
            assertThat(cmd.partidas().get(1).haber()).isEqualByComparingTo("100.00");
        }

        @Test
        @DisplayName("moneda null → fallback a cuentas en Bs (no rompe)")
        void deposito_sin_moneda_usa_bs() {
            adapter.registrarDeposito(cuentaSinMoneda, depositoBs);

            RegistrarAsientoCommand cmd = capturarComando();
            assertThat(cmd.partidas().get(0).codigoCuenta()).isEqualTo("1.1.01");
            assertThat(cmd.partidas().get(1).codigoCuenta()).isEqualTo("2.1.01");
        }

        @Test
        @DisplayName("glosa del asiento incluye número de operación y cuenta")
        void glosa_incluye_metadata_del_movimiento() {
            adapter.registrarDeposito(cuentaBs, depositoBs);

            RegistrarAsientoCommand cmd = capturarComando();
            assertThat(cmd.glosa())
                    .contains("MOV-2026-000001")
                    .contains("AHO-2026-000001");
        }

        @Test
        @DisplayName("fecha contable es hoy (LocalDate.now)")
        void fecha_contable_es_hoy() {
            adapter.registrarDeposito(cuentaBs, depositoBs);

            RegistrarAsientoCommand cmd = capturarComando();
            assertThat(cmd.fechaContable()).isEqualTo(java.time.LocalDate.now());
        }
    }

    // ─── Retiro ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Retiro → asiento DEBE pasivo / HABER activo (espejo del depósito)")
    class Retiro {

        @Test
        @DisplayName("Bs: DEBE 2.1.01 (baja captación) / HABER 1.1.01 (sale efectivo)")
        void retiro_bs_invierte_cuentas_vs_deposito() {
            adapter.registrarRetiro(cuentaBs, retiroBs);

            RegistrarAsientoCommand cmd = capturarComando();
            assertThat(cmd.origen()).isEqualTo(OrigenAsiento.AHORRO_RETIRO);
            assertThat(cmd.partidas()).hasSize(2);

            // DEBE el PASIVO (en retiro la obligación con el socio baja)
            var debe = cmd.partidas().get(0);
            assertThat(debe.codigoCuenta()).isEqualTo("2.1.01");
            assertThat(debe.debe()).isEqualByComparingTo("250.00");

            // HABER el ACTIVO (sale efectivo)
            var haber = cmd.partidas().get(1);
            assertThat(haber.codigoCuenta()).isEqualTo("1.1.01");
            assertThat(haber.haber()).isEqualByComparingTo("250.00");
        }

        @Test
        @DisplayName("USD: DEBE 2.1.02 / HABER 1.1.05")
        void retiro_usd_usa_cuentas_usd_invertidas() {
            adapter.registrarRetiro(cuentaUsd, retiroBs);

            RegistrarAsientoCommand cmd = capturarComando();
            assertThat(cmd.partidas().get(0).codigoCuenta()).isEqualTo("2.1.02");
            assertThat(cmd.partidas().get(1).codigoCuenta()).isEqualTo("1.1.05");
        }
    }

    // ─── Propagación de errores ────────────────────────────────────────────

    @Test
    @DisplayName("excepción de AsientoContableService se propaga (NO se traga)")
    void error_contable_se_propaga_para_rollback() {
        doThrow(new AsientoContableException("cuenta inexistente: 1.1.01"))
                .when(asientoContableService).registrar(org.mockito.ArgumentMatchers.any());

        assertThatThrownBy(() -> adapter.registrarDeposito(cuentaBs, depositoBs))
                .isInstanceOf(AsientoContableException.class)
                .hasMessageContaining("1.1.01");
    }

    @Test
    @DisplayName("monto con muchos decimales se mantiene tal cual (validación queda en dominio)")
    void monto_decimales_se_propaga_sin_redondeo() {
        Movimiento m = Movimiento.builder()
                .id(UUID.randomUUID())
                .numeroOperacion("MOV-2026-0099")
                .monto(new BigDecimal("0.0001"))
                .build();

        adapter.registrarDeposito(cuentaBs, m);

        RegistrarAsientoCommand cmd = capturarComando();
        assertThat(cmd.partidas().get(0).debe()).isEqualByComparingTo("0.0001");
        assertThat(cmd.partidas().get(1).haber()).isEqualByComparingTo("0.0001");
    }

    @Test
    @DisplayName("misma partida ambas con el mismo monto (invariante partida doble pre-condición)")
    void debe_y_haber_son_del_mismo_monto() {
        adapter.registrarDeposito(cuentaBs, depositoBs);

        RegistrarAsientoCommand cmd = capturarComando();
        BigDecimal debe = cmd.partidas().get(0).debe();
        BigDecimal haber = cmd.partidas().get(1).haber();
        assertThat(debe).isEqualByComparingTo(haber);
    }

    // ─── Helper ────────────────────────────────────────────────────────────

    private RegistrarAsientoCommand capturarComando() {
        ArgumentCaptor<RegistrarAsientoCommand> captor =
                ArgumentCaptor.forClass(RegistrarAsientoCommand.class);
        verify(asientoContableService).registrar(captor.capture());
        return captor.getValue();
    }
}
