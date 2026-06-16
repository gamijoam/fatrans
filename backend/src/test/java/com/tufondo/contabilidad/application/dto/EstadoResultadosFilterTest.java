package com.tufondo.contabilidad.application.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EstadoResultadosFilterTest {

    @Test
    @DisplayName("filtro válido con factory de()")
    void filtro_valido() {
        var f = EstadoResultadosFilter.de(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31));
        assertThat(f.desde()).isEqualTo(LocalDate.of(2026, 5, 1));
        assertThat(f.hasta()).isEqualTo(LocalDate.of(2026, 5, 31));
        assertThat(f.incluirCeros()).isFalse();
    }

    @Test
    @DisplayName("rango > 366 días rechazado")
    void rango_excesivo_rechazado() {
        assertThatThrownBy(() -> EstadoResultadosFilter.de(
                LocalDate.of(2026, 1, 1), LocalDate.of(2028, 1, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("excede el máximo");
    }

    @Test
    @DisplayName("hasta < desde rechazado")
    void rango_invertido_rechazado() {
        assertThatThrownBy(() -> EstadoResultadosFilter.de(
                LocalDate.of(2026, 5, 31), LocalDate.of(2026, 5, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("anterior");
    }

    @Test
    @DisplayName("fechas null rechazadas")
    void fechas_null() {
        assertThatThrownBy(() -> new EstadoResultadosFilter(null, LocalDate.now(), false))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new EstadoResultadosFilter(LocalDate.now(), null, false))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("rango 366 días (año bisiesto) aceptado")
    void rango_366_aceptado() {
        var f = new EstadoResultadosFilter(
                LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 2), false);
        assertThat(f).isNotNull();
    }
}
