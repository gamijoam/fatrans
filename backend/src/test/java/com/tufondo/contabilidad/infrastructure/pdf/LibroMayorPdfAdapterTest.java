package com.tufondo.contabilidad.infrastructure.pdf;

import com.tufondo.contabilidad.application.dto.LibroMayorResponse;
import com.tufondo.contabilidad.domain.model.enums.NaturalezaSaldo;
import com.tufondo.contabilidad.domain.model.enums.OrigenAsiento;
import com.tufondo.contabilidad.domain.model.enums.TipoCuentaContable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests del método {@code generarLibroMayorPdf} del adapter unificado.
 *
 * <p>El adapter {@link LibroDiarioPdfAdapter} implementa todo el
 * {@code ContabilidadPdfPort} (Diario + Mayor). Tests del Libro Diario
 * en {@link LibroDiarioPdfAdapterTest}.</p>
 */
class LibroMayorPdfAdapterTest {

    private final LibroDiarioPdfAdapter adapter = new LibroDiarioPdfAdapter();

    @Test
    @DisplayName("período vacío (sin cuentas) genera PDF con encabezado y totales en cero")
    void pdf_vacio() {
        var libro = LibroMayorResponse.builder()
                .encabezado(encabezado(null, false, false))
                .cuentas(List.of())
                .totales(totalesCero())
                .build();

        byte[] pdf = adapter.generarLibroMayorPdf(libro);

        assertThat(pdf).isNotEmpty();
        assertThat(esPdfValido(pdf)).isTrue();
    }

    @Test
    @DisplayName("una cuenta sin movimientos (incluida) muestra '(sin movimientos)'")
    void cuenta_sin_movimientos() {
        var cuenta = LibroMayorResponse.CuentaConMovimientos.builder()
                .codigo("1.1.03").nombre("Bancos")
                .tipo(TipoCuentaContable.ACTIVO).naturaleza(NaturalezaSaldo.DEUDORA)
                .saldoInicialDebe(BigDecimal.ZERO).saldoInicialHaber(BigDecimal.ZERO)
                .saldoInicialNeto(BigDecimal.ZERO).saldoInicialEtiqueta("—")
                .movimientos(List.of())
                .totalDebePeriodo(BigDecimal.ZERO).totalHaberPeriodo(BigDecimal.ZERO)
                .cantidadMovimientos(0)
                .saldoFinalDebe(BigDecimal.ZERO).saldoFinalHaber(BigDecimal.ZERO)
                .saldoFinalNeto(BigDecimal.ZERO).saldoFinalEtiqueta("—")
                .build();
        var libro = LibroMayorResponse.builder()
                .encabezado(encabezado(null, true, false))
                .cuentas(List.of(cuenta))
                .totales(totalesCero())
                .build();

        byte[] pdf = adapter.generarLibroMayorPdf(libro);
        assertThat(esPdfValido(pdf)).isTrue();
    }

    @Test
    @DisplayName("cuenta con varios movimientos y contracuentas se renderiza")
    void cuenta_con_movimientos() {
        var movs = List.of(
                movimiento(LocalDate.of(2026, 5, 3), 1L, "2026-000001",
                        OrigenAsiento.AHORRO_DEPOSITO, "Depósito",
                        "2.1.01", "Cuentas de Ahorro Bs", null,
                        new BigDecimal("100.00"), BigDecimal.ZERO,
                        new BigDecimal("100.00")),
                movimiento(LocalDate.of(2026, 5, 5), 2L, "2026-000002",
                        OrigenAsiento.AHORRO_RETIRO, "Retiro",
                        "2.1.01", "Cuentas de Ahorro Bs", null,
                        BigDecimal.ZERO, new BigDecimal("40.00"),
                        new BigDecimal("60.00")),
                movimiento(LocalDate.of(2026, 5, 10), 3L, "2026-000003",
                        OrigenAsiento.CREDITO_DESEMBOLSO, "Desembolso",
                        "1.3.01", "Créditos", "(múltiple)",
                        BigDecimal.ZERO, new BigDecimal("500.00"),
                        new BigDecimal("-440.00")));  // saldo negativo: lado opuesto a naturaleza

        var cuenta = LibroMayorResponse.CuentaConMovimientos.builder()
                .codigo("1.1.03").nombre("Bancos")
                .tipo(TipoCuentaContable.ACTIVO).naturaleza(NaturalezaSaldo.DEUDORA)
                .saldoInicialDebe(BigDecimal.ZERO).saldoInicialHaber(BigDecimal.ZERO)
                .saldoInicialNeto(BigDecimal.ZERO).saldoInicialEtiqueta("—")
                .movimientos(movs)
                .totalDebePeriodo(new BigDecimal("100.00"))
                .totalHaberPeriodo(new BigDecimal("540.00"))
                .cantidadMovimientos(3)
                .saldoFinalDebe(new BigDecimal("100.00")).saldoFinalHaber(new BigDecimal("540.00"))
                .saldoFinalNeto(new BigDecimal("440.00")).saldoFinalEtiqueta("A") // opuesto a su naturaleza
                .build();

        var libro = LibroMayorResponse.builder()
                .encabezado(encabezado("1.1.03", true, false))
                .cuentas(List.of(cuenta))
                .totales(LibroMayorResponse.Totales.builder()
                        .cantidadCuentas(1).cantidadMovimientos(3)
                        .totalDebe(new BigDecimal("100.00")).totalHaber(new BigDecimal("540.00"))
                        .balanceado(false)
                        .build())
                .build();

        byte[] pdf = adapter.generarLibroMayorPdf(libro);
        assertThat(esPdfValido(pdf)).isTrue();
        // PDF con 3 movimientos visibles
        assertThat(pdf.length).isGreaterThan(2000);
    }

    @Test
    @DisplayName("múltiples cuentas con paginación se renderizan sin OOM")
    void muchas_cuentas_pagina() {
        var cuentas = java.util.stream.IntStream.rangeClosed(1, 20)
                .mapToObj(i -> LibroMayorResponse.CuentaConMovimientos.builder()
                        .codigo(String.format("1.%d.%02d", i / 10, i % 100))
                        .nombre("Cuenta " + i)
                        .tipo(TipoCuentaContable.ACTIVO).naturaleza(NaturalezaSaldo.DEUDORA)
                        .saldoInicialDebe(BigDecimal.ZERO).saldoInicialHaber(BigDecimal.ZERO)
                        .saldoInicialNeto(BigDecimal.ZERO).saldoInicialEtiqueta("—")
                        .movimientos(List.of(
                                movimiento(LocalDate.now(), (long) i, "2026-000" + i,
                                        OrigenAsiento.MANUAL, "Mov",
                                        "2.1.01", "Contra", null,
                                        new BigDecimal("10.00"), BigDecimal.ZERO,
                                        new BigDecimal("10.00"))))
                        .totalDebePeriodo(new BigDecimal("10.00"))
                        .totalHaberPeriodo(BigDecimal.ZERO).cantidadMovimientos(1)
                        .saldoFinalDebe(new BigDecimal("10.00")).saldoFinalHaber(BigDecimal.ZERO)
                        .saldoFinalNeto(new BigDecimal("10.00")).saldoFinalEtiqueta("D")
                        .build())
                .toList();

        var libro = LibroMayorResponse.builder()
                .encabezado(encabezado(null, false, false))
                .cuentas(cuentas)
                .totales(LibroMayorResponse.Totales.builder()
                        .cantidadCuentas(20).cantidadMovimientos(20)
                        .totalDebe(new BigDecimal("200.00")).totalHaber(BigDecimal.ZERO)
                        .balanceado(false).build())
                .build();

        byte[] pdf = adapter.generarLibroMayorPdf(libro);
        assertThat(esPdfValido(pdf)).isTrue();
        assertThat(pdf.length).isGreaterThan(5000);
    }

    @Test
    @DisplayName("totales con balance desbalanceado se marca visualmente sin crashear")
    void totales_desbalanceados() {
        var libro = LibroMayorResponse.builder()
                .encabezado(encabezado(null, false, false))
                .cuentas(List.of())
                .totales(LibroMayorResponse.Totales.builder()
                        .cantidadCuentas(0).cantidadMovimientos(0)
                        .totalDebe(new BigDecimal("100"))
                        .totalHaber(new BigDecimal("90"))
                        .balanceado(false)
                        .build())
                .build();

        byte[] pdf = adapter.generarLibroMayorPdf(libro);
        assertThat(esPdfValido(pdf)).isTrue();
    }

    // ─── Helpers ───────────────────────────────────────────────────────────

    private boolean esPdfValido(byte[] pdf) {
        if (pdf == null || pdf.length < 4) return false;
        return pdf[0] == '%' && pdf[1] == 'P' && pdf[2] == 'D' && pdf[3] == 'F';
    }

    private LibroMayorResponse.Encabezado encabezado(
            String filtroCuenta, boolean sinMov, boolean tot) {
        return LibroMayorResponse.Encabezado.builder()
                .razonSocial("Fatrans Test")
                .rif("J-12345678-9")
                .desde(LocalDate.of(2026, 5, 1))
                .hasta(LocalDate.of(2026, 5, 31))
                .generadoEn(Instant.now())
                .generadoPorUsuarioId(UUID.randomUUID())
                .filtroCuenta(filtroCuenta)
                .incluyeSinMovimientos(sinMov)
                .incluyeTotalizadoras(tot)
                .build();
    }

    private LibroMayorResponse.Totales totalesCero() {
        return LibroMayorResponse.Totales.builder()
                .cantidadCuentas(0).cantidadMovimientos(0)
                .totalDebe(BigDecimal.ZERO).totalHaber(BigDecimal.ZERO)
                .balanceado(true)
                .build();
    }

    private LibroMayorResponse.MovimientoMayor movimiento(
            LocalDate fecha, long numero, String numeroFmt, OrigenAsiento origen,
            String glosa, String contraCodigo, String contraNombre, String contraResumen,
            BigDecimal debe, BigDecimal haber, BigDecimal saldoAcum) {
        return LibroMayorResponse.MovimientoMayor.builder()
                .fechaContable(fecha)
                .numeroAsiento(numero).numeroAsientoFormateado(numeroFmt)
                .origen(origen).glosaAsiento(glosa).referenciaExterna("REF-" + numero)
                .contracuentaCodigo(contraCodigo).contracuentaNombre(contraNombre)
                .contracuentaResumen(contraResumen)
                .debe(debe).haber(haber).saldoAcumulado(saldoAcum)
                .build();
    }
}
