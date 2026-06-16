package com.tufondo.contabilidad.application.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LibroMayorFilterTest {

    @Test
    @DisplayName("filtro válido 1 mes — todos los flags default")
    void filtro_valido_completo() {
        var f = LibroMayorFilter.completo(
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31));
        assertThat(f.codigoCuenta()).isNull();
        assertThat(f.incluirSinMovimientos()).isFalse();
        assertThat(f.incluirTotalizadoras()).isFalse();
        assertThat(f.filtraPorCuenta()).isFalse();
    }

    @Test
    @DisplayName("filtro por cuenta específica — flag detectado")
    void filtro_por_cuenta_detectado() {
        var f = LibroMayorFilter.deCuenta(
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31), "1.1.03");
        assertThat(f.filtraPorCuenta()).isTrue();
        assertThat(f.incluirSinMovimientos()).isTrue();
    }

    @Test
    @DisplayName("codigoCuenta null y vacío se tratan igual (no filtra)")
    void codigo_null_y_vacio_no_filtran() {
        var f1 = new LibroMayorFilter(LocalDate.now().minusDays(7), LocalDate.now(), null, false, false);
        var f2 = new LibroMayorFilter(LocalDate.now().minusDays(7), LocalDate.now(), "", false, false);
        var f3 = new LibroMayorFilter(LocalDate.now().minusDays(7), LocalDate.now(), "   ", false, false);
        assertThat(f1.filtraPorCuenta()).isFalse();
        assertThat(f2.filtraPorCuenta()).isFalse();
        assertThat(f3.filtraPorCuenta()).isFalse();
    }

    @Test
    @DisplayName("rango > 366 días → rechazado")
    void rango_excesivo_rechazado() {
        assertThatThrownBy(() -> new LibroMayorFilter(
                LocalDate.of(2026, 1, 1), LocalDate.of(2028, 1, 1),
                null, false, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("excede el máximo");
    }

    @Test
    @DisplayName("hasta < desde → rechazado")
    void rango_invertido_rechazado() {
        assertThatThrownBy(() -> new LibroMayorFilter(
                LocalDate.of(2026, 5, 31), LocalDate.of(2026, 5, 1),
                null, false, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("anterior");
    }

    @Test
    @DisplayName("fechas null → rechazadas")
    void fechas_null_rechazadas() {
        assertThatThrownBy(() -> new LibroMayorFilter(null, LocalDate.now(), null, false, false))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new LibroMayorFilter(LocalDate.now(), null, null, false, false))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("rango exactamente 366 días (año bisiesto) → OK")
    void rango_limite_366_aceptado() {
        var f = new LibroMayorFilter(
                LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 2),  // 366 días
                null, false, false);
        assertThat(f).isNotNull();
    }

    @Test
    @DisplayName("flags incluirSinMovimientos / incluirTotalizadoras se almacenan")
    void flags_se_almacenan() {
        var f = new LibroMayorFilter(
                LocalDate.now().minusDays(7), LocalDate.now(),
                null, true, true);
        assertThat(f.incluirSinMovimientos()).isTrue();
        assertThat(f.incluirTotalizadoras()).isTrue();
    }
}
