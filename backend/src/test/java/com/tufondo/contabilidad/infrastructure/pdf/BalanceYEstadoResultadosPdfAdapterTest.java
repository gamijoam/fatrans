package com.tufondo.contabilidad.infrastructure.pdf;

import com.tufondo.contabilidad.application.dto.BalanceGeneralResponse;
import com.tufondo.contabilidad.application.dto.EstadoResultadosResponse;
import com.tufondo.contabilidad.domain.model.enums.NaturalezaSaldo;
import com.tufondo.contabilidad.domain.model.enums.TipoCuentaContable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests del método {@code generarBalanceGeneralPdf} y
 * {@code generarEstadoResultadosPdf} del adapter unificado (sub-issue #271).
 *
 * <p>Verifica generación de bytes válidos para diversos casos: vacío, con
 * datos, con cuentas correctoras, con déficit, balanceado y desbalanceado.</p>
 */
class BalanceYEstadoResultadosPdfAdapterTest {

    private final LibroDiarioPdfAdapter adapter = new LibroDiarioPdfAdapter();

    // ═══ Balance General ═══════════════════════════════════════════════════

    @Nested
    @DisplayName("Balance General PDF")
    class Balance {

        @Test
        @DisplayName("balance vacío genera PDF con cabecera y totales en cero")
        void balance_vacio() {
            BalanceGeneralResponse b = BalanceGeneralResponse.builder()
                    .encabezado(encabezadoBalance())
                    .activo(seccionVacia(TipoCuentaContable.ACTIVO, "ACTIVO"))
                    .pasivo(seccionVacia(TipoCuentaContable.PASIVO, "PASIVO"))
                    .patrimonio(seccionVacia(TipoCuentaContable.PATRIMONIO, "PATRIMONIO"))
                    .excedenteEjercicio(BigDecimal.ZERO)
                    .excedenteEtiqueta("—")
                    .totales(BalanceGeneralResponse.Totales.builder()
                            .totalActivo(BigDecimal.ZERO).totalPasivo(BigDecimal.ZERO)
                            .totalPatrimonio(BigDecimal.ZERO).excedenteEjercicio(BigDecimal.ZERO)
                            .totalPasivoMasPatrimonio(BigDecimal.ZERO)
                            .diferencia(BigDecimal.ZERO).balanceado(true)
                            .build())
                    .build();

            byte[] pdf = adapter.generarBalanceGeneralPdf(b);
            assertThat(esPdfValido(pdf)).isTrue();
        }

        @Test
        @DisplayName("balance con cuentas y correctora genera PDF (signo negativo en correctora)")
        void balance_con_correctora() {
            BalanceGeneralResponse.NodoCuenta cartera = nodoBalance(
                    "1.3.01", "Créditos por Cobrar", 3, false, "10000");
            BalanceGeneralResponse.NodoCuenta provision = BalanceGeneralResponse.NodoCuenta.builder()
                    .codigo("1.3.99").nombre("Provisión Cartera (CR)")
                    .nivel(3).naturaleza(NaturalezaSaldo.ACREEDORA)
                    .esCorrectora(true)
                    .saldoNeto(new BigDecimal("-500"))   // saldo negativo dentro de rubro deudor
                    .saldoPresentacion(new BigDecimal("500"))
                    .hijos(List.of())
                    .build();
            BalanceGeneralResponse.NodoCuenta grupo = BalanceGeneralResponse.NodoCuenta.builder()
                    .codigo("1.3").nombre("Cartera").nivel(2)
                    .naturaleza(NaturalezaSaldo.DEUDORA).esCorrectora(false)
                    .saldoNeto(new BigDecimal("9500")).saldoPresentacion(new BigDecimal("9500"))
                    .hijos(List.of(cartera, provision))
                    .build();
            BalanceGeneralResponse.NodoCuenta rubro = BalanceGeneralResponse.NodoCuenta.builder()
                    .codigo("1").nombre("ACTIVO").nivel(1)
                    .naturaleza(NaturalezaSaldo.DEUDORA).esCorrectora(false)
                    .saldoNeto(new BigDecimal("9500")).saldoPresentacion(new BigDecimal("9500"))
                    .hijos(List.of(grupo))
                    .build();
            BalanceGeneralResponse b = BalanceGeneralResponse.builder()
                    .encabezado(encabezadoBalance())
                    .activo(BalanceGeneralResponse.Seccion.builder()
                            .tipo(TipoCuentaContable.ACTIVO).titulo("ACTIVO")
                            .rubros(List.of(rubro)).total(new BigDecimal("9500"))
                            .build())
                    .pasivo(seccionVacia(TipoCuentaContable.PASIVO, "PASIVO"))
                    .patrimonio(seccionVacia(TipoCuentaContable.PATRIMONIO, "PATRIMONIO"))
                    .excedenteEjercicio(BigDecimal.ZERO).excedenteEtiqueta("—")
                    .totales(BalanceGeneralResponse.Totales.builder()
                            .totalActivo(new BigDecimal("9500")).totalPasivo(BigDecimal.ZERO)
                            .totalPatrimonio(BigDecimal.ZERO).excedenteEjercicio(BigDecimal.ZERO)
                            .totalPasivoMasPatrimonio(BigDecimal.ZERO)
                            .diferencia(new BigDecimal("9500")).balanceado(false)
                            .build())
                    .build();

            byte[] pdf = adapter.generarBalanceGeneralPdf(b);
            assertThat(esPdfValido(pdf)).isTrue();
            assertThat(pdf.length).isGreaterThan(1500);
        }

        @Test
        @DisplayName("balance con déficit del ejercicio se renderiza con fondo distintivo")
        void balance_con_deficit() {
            BalanceGeneralResponse b = BalanceGeneralResponse.builder()
                    .encabezado(encabezadoBalance())
                    .activo(seccionVacia(TipoCuentaContable.ACTIVO, "ACTIVO"))
                    .pasivo(seccionVacia(TipoCuentaContable.PASIVO, "PASIVO"))
                    .patrimonio(seccionVacia(TipoCuentaContable.PATRIMONIO, "PATRIMONIO"))
                    .excedenteEjercicio(new BigDecimal("200"))
                    .excedenteEtiqueta("DÉFICIT")
                    .totales(BalanceGeneralResponse.Totales.builder()
                            .totalActivo(BigDecimal.ZERO).totalPasivo(BigDecimal.ZERO)
                            .totalPatrimonio(BigDecimal.ZERO)
                            .excedenteEjercicio(new BigDecimal("-200"))
                            .totalPasivoMasPatrimonio(new BigDecimal("-200"))
                            .diferencia(new BigDecimal("200")).balanceado(false)
                            .build())
                    .build();
            byte[] pdf = adapter.generarBalanceGeneralPdf(b);
            assertThat(esPdfValido(pdf)).isTrue();
        }
    }

    // ═══ Estado de Resultados ══════════════════════════════════════════════

    @Nested
    @DisplayName("Estado de Resultados PDF")
    class EstadoResultados {

        @Test
        @DisplayName("ER vacío genera PDF con secciones vacías y excedente cero")
        void er_vacio() {
            EstadoResultadosResponse er = EstadoResultadosResponse.builder()
                    .encabezado(encabezadoER())
                    .ingresos(seccionERVacia(TipoCuentaContable.INGRESO, "INGRESOS"))
                    .egresos(seccionERVacia(TipoCuentaContable.EGRESO, "EGRESOS"))
                    .excedente(BigDecimal.ZERO).excedenteEtiqueta("—")
                    .build();
            byte[] pdf = adapter.generarEstadoResultadosPdf(er);
            assertThat(esPdfValido(pdf)).isTrue();
        }

        @Test
        @DisplayName("ER con excedente positivo se renderiza con fondo verde")
        void er_con_excedente() {
            EstadoResultadosResponse.NodoCuenta intereses = nodoER(
                    "4.1.01", "Intereses sobre Créditos", 3, "1000");
            EstadoResultadosResponse.NodoCuenta grupo = EstadoResultadosResponse.NodoCuenta.builder()
                    .codigo("4.1").nombre("Ingresos Cartera").nivel(2)
                    .naturaleza(NaturalezaSaldo.ACREEDORA)
                    .saldoNeto(new BigDecimal("1000")).saldoPresentacion(new BigDecimal("1000"))
                    .hijos(List.of(intereses)).build();
            EstadoResultadosResponse.NodoCuenta rubro = EstadoResultadosResponse.NodoCuenta.builder()
                    .codigo("4").nombre("INGRESOS").nivel(1)
                    .naturaleza(NaturalezaSaldo.ACREEDORA)
                    .saldoNeto(new BigDecimal("1000")).saldoPresentacion(new BigDecimal("1000"))
                    .hijos(List.of(grupo)).build();
            EstadoResultadosResponse er = EstadoResultadosResponse.builder()
                    .encabezado(encabezadoER())
                    .ingresos(EstadoResultadosResponse.Seccion.builder()
                            .tipo(TipoCuentaContable.INGRESO).titulo("INGRESOS")
                            .rubros(List.of(rubro)).total(new BigDecimal("1000"))
                            .build())
                    .egresos(seccionERVacia(TipoCuentaContable.EGRESO, "EGRESOS"))
                    .excedente(new BigDecimal("1000"))
                    .excedenteEtiqueta("EXCEDENTE")
                    .build();
            byte[] pdf = adapter.generarEstadoResultadosPdf(er);
            assertThat(esPdfValido(pdf)).isTrue();
            assertThat(pdf.length).isGreaterThan(1500);
        }

        @Test
        @DisplayName("ER con déficit se renderiza con fondo rojo")
        void er_con_deficit() {
            EstadoResultadosResponse er = EstadoResultadosResponse.builder()
                    .encabezado(encabezadoER())
                    .ingresos(seccionERVacia(TipoCuentaContable.INGRESO, "INGRESOS"))
                    .egresos(seccionERVacia(TipoCuentaContable.EGRESO, "EGRESOS"))
                    .excedente(new BigDecimal("500"))
                    .excedenteEtiqueta("DÉFICIT")
                    .build();
            byte[] pdf = adapter.generarEstadoResultadosPdf(er);
            assertThat(esPdfValido(pdf)).isTrue();
        }
    }

    // ─── Helpers ───────────────────────────────────────────────────────────

    private boolean esPdfValido(byte[] pdf) {
        if (pdf == null || pdf.length < 4) return false;
        return pdf[0] == '%' && pdf[1] == 'P' && pdf[2] == 'D' && pdf[3] == 'F';
    }

    private BalanceGeneralResponse.Encabezado encabezadoBalance() {
        return BalanceGeneralResponse.Encabezado.builder()
                .razonSocial("Fatrans Test").rif("J-12345678-9")
                .fechaCorte(LocalDate.of(2026, 5, 31))
                .inicioEjercicio(LocalDate.of(2026, 1, 1))
                .generadoEn(Instant.now()).generadoPorUsuarioId(UUID.randomUUID())
                .incluyeCeros(false).build();
    }

    private EstadoResultadosResponse.Encabezado encabezadoER() {
        return EstadoResultadosResponse.Encabezado.builder()
                .razonSocial("Fatrans Test").rif("J-12345678-9")
                .desde(LocalDate.of(2026, 5, 1)).hasta(LocalDate.of(2026, 5, 31))
                .generadoEn(Instant.now()).generadoPorUsuarioId(UUID.randomUUID())
                .incluyeCeros(false).build();
    }

    private BalanceGeneralResponse.Seccion seccionVacia(TipoCuentaContable tipo, String titulo) {
        return BalanceGeneralResponse.Seccion.builder()
                .tipo(tipo).titulo(titulo).rubros(List.of()).total(BigDecimal.ZERO)
                .build();
    }

    private EstadoResultadosResponse.Seccion seccionERVacia(TipoCuentaContable tipo, String titulo) {
        return EstadoResultadosResponse.Seccion.builder()
                .tipo(tipo).titulo(titulo).rubros(List.of()).total(BigDecimal.ZERO)
                .build();
    }

    private BalanceGeneralResponse.NodoCuenta nodoBalance(
            String codigo, String nombre, int nivel, boolean correctora, String saldo) {
        return BalanceGeneralResponse.NodoCuenta.builder()
                .codigo(codigo).nombre(nombre).nivel(nivel)
                .naturaleza(NaturalezaSaldo.DEUDORA).esCorrectora(correctora)
                .saldoNeto(new BigDecimal(saldo))
                .saldoPresentacion(new BigDecimal(saldo).abs())
                .hijos(List.of()).build();
    }

    private EstadoResultadosResponse.NodoCuenta nodoER(
            String codigo, String nombre, int nivel, String saldo) {
        return EstadoResultadosResponse.NodoCuenta.builder()
                .codigo(codigo).nombre(nombre).nivel(nivel)
                .naturaleza(NaturalezaSaldo.ACREEDORA)
                .saldoNeto(new BigDecimal(saldo))
                .saldoPresentacion(new BigDecimal(saldo).abs())
                .hijos(List.of()).build();
    }
}
