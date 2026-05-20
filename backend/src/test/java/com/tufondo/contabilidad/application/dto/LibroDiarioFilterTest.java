package com.tufondo.contabilidad.application.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests del DTO {@link LibroDiarioFilter} — validaciones de rango.
 */
class LibroDiarioFilterTest {

    @Test
    @DisplayName("filtro válido (1 mes) → OK")
    void filtro_un_mes_valido() {
        var f = new LibroDiarioFilter(
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                true);
        assertThat(f.desde()).isEqualTo(LocalDate.of(2026, 5, 1));
        assertThat(f.hasta()).isEqualTo(LocalDate.of(2026, 5, 31));
        assertThat(f.incluirAnulados()).isTrue();
    }

    @Test
    @DisplayName("filtro 366 días (año bisiesto) → OK (límite máximo)")
    void rango_366_dias_es_aceptado() {
        // 2024 fue bisiesto: 366 días entre 2024-01-01 y 2024-12-31 = 365 días.
        // Verifico el máximo absoluto del constructor (366).
        var f = new LibroDiarioFilter(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2027, 1, 2),  // exactamente 366 días después
                true);
        assertThat(f).isNotNull();
    }

    @Test
    @DisplayName("rango > 366 días → rechazado")
    void rango_excesivo_rechazado() {
        assertThatThrownBy(() -> new LibroDiarioFilter(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2028, 1, 1),  // 2 años
                true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("excede el máximo");
    }

    @Test
    @DisplayName("hasta < desde → rechazado")
    void rango_invertido_rechazado() {
        assertThatThrownBy(() -> new LibroDiarioFilter(
                LocalDate.of(2026, 5, 31),
                LocalDate.of(2026, 5, 1),
                true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("anterior");
    }

    @Test
    @DisplayName("desde o hasta null → rechazado")
    void fechas_null_rechazado() {
        assertThatThrownBy(() -> new LibroDiarioFilter(null, LocalDate.now(), true))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new LibroDiarioFilter(LocalDate.now(), null, true))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("factory de() crea filtro con incluirAnulados=true")
    void factory_de_default() {
        var f = LibroDiarioFilter.de(LocalDate.now().minusDays(7), LocalDate.now());
        assertThat(f.incluirAnulados()).isTrue();
    }

    @Test
    @DisplayName("desde = hasta (mismo día) → OK (período de 1 día)")
    void rango_un_dia_valido() {
        var d = LocalDate.of(2026, 5, 20);
        var f = new LibroDiarioFilter(d, d, false);
        assertThat(f.desde()).isEqualTo(f.hasta());
    }
}
