package com.tufondo.contabilidad.infrastructure.pdf;

import com.tufondo.contabilidad.application.dto.LibroDiarioResponse;
import com.tufondo.contabilidad.domain.model.enums.EstadoAsiento;
import com.tufondo.contabilidad.domain.model.enums.OrigenAsiento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests del {@link LibroDiarioPdfAdapter}.
 *
 * <p>Verifica que el PDF se genera (bytes válidos) sin crashear ante
 * variaciones: período vacío, asientos anulados, muchas filas, decimales,
 * desbalance (caso defensivo).</p>
 */
class LibroDiarioPdfAdapterTest {

    private final LibroDiarioPdfAdapter adapter = new LibroDiarioPdfAdapter();

    @Test
    @DisplayName("período vacío genera PDF con cabecera y totales en cero")
    void pdf_periodo_vacio() {
        byte[] pdf = adapter.generarLibroDiarioPdf(libroVacio());

        assertThat(pdf).isNotEmpty();
        assertThat(esPdfValido(pdf)).isTrue();
        assertThat(pdf.length).isGreaterThan(500); // al menos cabecera + estructura mínima
    }

    @Test
    @DisplayName("PDF con 1 asiento normal — bytes válidos y mayor que el vacío")
    void pdf_un_asiento() {
        var libro = libroConAsientos(List.of(asientoNormal(1, "2026-000001")));
        byte[] pdf = adapter.generarLibroDiarioPdf(libro);

        assertThat(esPdfValido(pdf)).isTrue();
        assertThat(pdf.length).isGreaterThan(1000);
    }

    @Test
    @DisplayName("PDF con asiento ANULADO se genera (marca visual no rompe)")
    void pdf_con_anulado() {
        var libro = libroConAsientos(List.of(
                asientoNormal(1, "2026-000001"),
                asientoAnulado(2, "2026-000002")));
        byte[] pdf = adapter.generarLibroDiarioPdf(libro);

        assertThat(esPdfValido(pdf)).isTrue();
    }

    @Test
    @DisplayName("PDF con muchos asientos (paginación) genera sin OOM")
    void pdf_muchos_asientos_pagina() {
        // 50 asientos cada uno con 2 partidas → ~100 filas; debe paginar
        var asientos = java.util.stream.IntStream.rangeClosed(1, 50)
                .mapToObj(i -> asientoNormal((long) i, String.format("2026-%06d", i)))
                .toList();
        var libro = libroConAsientos(asientos);

        byte[] pdf = adapter.generarLibroDiarioPdf(libro);

        assertThat(esPdfValido(pdf)).isTrue();
        // PDF con 50 asientos debe ser visiblemente más grande
        assertThat(pdf.length).isGreaterThan(5000);
    }

    @Test
    @DisplayName("PDF con desbalance (caso defensivo) marca visualmente sin crashear")
    void pdf_desbalance_se_renderiza() {
        // Simular un desbalance manual en los totales para asegurar que el adapter
        // no asume balance; solo lo marca visualmente.
        var enc = encabezado();
        var asiento = asientoNormal(1, "2026-000001");
        var totalesDesbalanceados = LibroDiarioResponse.Totales.builder()
                .cantidadAsientos(1)
                .cantidadAnulados(0)
                .totalDebe(new BigDecimal("100.00"))
                .totalHaber(new BigDecimal("90.00"))  // diferencia
                .balanceado(false)
                .build();
        var libro = LibroDiarioResponse.builder()
                .encabezado(enc)
                .asientos(List.of(asiento))
                .totales(totalesDesbalanceados)
                .build();

        byte[] pdf = adapter.generarLibroDiarioPdf(libro);
        assertThat(esPdfValido(pdf)).isTrue();
    }

    // ─── Helpers ───────────────────────────────────────────────────────────

    /** Verifica que el byte array empiece con "%PDF" — header de PDF. */
    private boolean esPdfValido(byte[] pdf) {
        if (pdf == null || pdf.length < 4) return false;
        return pdf[0] == '%' && pdf[1] == 'P' && pdf[2] == 'D' && pdf[3] == 'F';
    }

    private LibroDiarioResponse libroVacio() {
        return LibroDiarioResponse.builder()
                .encabezado(encabezado())
                .asientos(List.of())
                .totales(LibroDiarioResponse.Totales.builder()
                        .cantidadAsientos(0)
                        .cantidadAnulados(0)
                        .totalDebe(BigDecimal.ZERO)
                        .totalHaber(BigDecimal.ZERO)
                        .balanceado(true)
                        .build())
                .build();
    }

    private LibroDiarioResponse libroConAsientos(List<LibroDiarioResponse.AsientoDiario> asientos) {
        BigDecimal totalDebe = asientos.stream()
                .map(LibroDiarioResponse.AsientoDiario::totalDebe)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalHaber = asientos.stream()
                .map(LibroDiarioResponse.AsientoDiario::totalHaber)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int anulados = (int) asientos.stream()
                .filter(a -> a.estado() == EstadoAsiento.ANULADO).count();
        return LibroDiarioResponse.builder()
                .encabezado(encabezado())
                .asientos(asientos)
                .totales(LibroDiarioResponse.Totales.builder()
                        .cantidadAsientos(asientos.size())
                        .cantidadAnulados(anulados)
                        .totalDebe(totalDebe)
                        .totalHaber(totalHaber)
                        .balanceado(totalDebe.compareTo(totalHaber) == 0)
                        .build())
                .build();
    }

    private LibroDiarioResponse.Encabezado encabezado() {
        return LibroDiarioResponse.Encabezado.builder()
                .razonSocial("Fatrans Test")
                .rif("J-12345678-9")
                .desde(LocalDate.of(2026, 5, 1))
                .hasta(LocalDate.of(2026, 5, 31))
                .generadoEn(Instant.now())
                .generadoPorUsuarioId(UUID.randomUUID())
                .incluyeAnulados(true)
                .build();
    }

    private LibroDiarioResponse.AsientoDiario asientoNormal(long numero, String formateado) {
        return LibroDiarioResponse.AsientoDiario.builder()
                .numero(numero)
                .numeroFormateado(formateado)
                .fechaContable(LocalDate.of(2026, 5, 10))
                .origen(OrigenAsiento.AHORRO_DEPOSITO)
                .estado(EstadoAsiento.REGISTRADO)
                .glosa("Depósito de prueba " + numero)
                .referenciaExterna("MOV-" + numero)
                .motivoAnulacion(null)
                .totalDebe(new BigDecimal("100.00"))
                .totalHaber(new BigDecimal("100.00"))
                .partidas(List.of(
                        LibroDiarioResponse.PartidaDiario.builder()
                                .codigoCuenta("1.1.03")
                                .nombreCuenta("Bancos Cta Corriente Bs")
                                .debe(new BigDecimal("100.00"))
                                .haber(BigDecimal.ZERO)
                                .glosa("Ingreso")
                                .orden(1)
                                .build(),
                        LibroDiarioResponse.PartidaDiario.builder()
                                .codigoCuenta("2.1.01")
                                .nombreCuenta("Cuentas de Ahorro Bs")
                                .debe(BigDecimal.ZERO)
                                .haber(new BigDecimal("100.00"))
                                .glosa("Crédito ahorro")
                                .orden(2)
                                .build()))
                .build();
    }

    private LibroDiarioResponse.AsientoDiario asientoAnulado(long numero, String formateado) {
        return LibroDiarioResponse.AsientoDiario.builder()
                .numero(numero)
                .numeroFormateado(formateado)
                .fechaContable(LocalDate.of(2026, 5, 12))
                .origen(OrigenAsiento.AHORRO_RETIRO)
                .estado(EstadoAsiento.ANULADO)
                .glosa("Retiro que se anuló")
                .referenciaExterna("MOV-" + numero)
                .motivoAnulacion("Error en el monto registrado")
                .totalDebe(new BigDecimal("250.00"))
                .totalHaber(new BigDecimal("250.00"))
                .partidas(List.of(
                        LibroDiarioResponse.PartidaDiario.builder()
                                .codigoCuenta("2.1.01")
                                .nombreCuenta("Cuentas de Ahorro Bs")
                                .debe(new BigDecimal("250.00"))
                                .haber(BigDecimal.ZERO)
                                .glosa("")
                                .orden(1)
                                .build(),
                        LibroDiarioResponse.PartidaDiario.builder()
                                .codigoCuenta("1.1.03")
                                .nombreCuenta("Bancos Cta Corriente Bs")
                                .debe(BigDecimal.ZERO)
                                .haber(new BigDecimal("250.00"))
                                .glosa("")
                                .orden(2)
                                .build()))
                .build();
    }
}
