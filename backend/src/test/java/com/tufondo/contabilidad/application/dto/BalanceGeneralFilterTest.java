package com.tufondo.contabilidad.application.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BalanceGeneralFilterTest {

    @Test
    @DisplayName("filtro válido con fecha de corte")
    void filtro_valido() {
        var f = BalanceGeneralFilter.al(LocalDate.of(2026, 5, 31));
        assertThat(f.fechaCorte()).isEqualTo(LocalDate.of(2026, 5, 31));
        assertThat(f.incluirCeros()).isFalse();
        assertThat(f.inicioEjercicio()).isNull();
    }

    @Test
    @DisplayName("inicioEjercicio default = 1-enero del año de fechaCorte")
    void inicio_default_enero_mismo_anio() {
        var f = BalanceGeneralFilter.al(LocalDate.of(2026, 5, 31));
        assertThat(f.inicioEjercicioResuelto()).isEqualTo(LocalDate.of(2026, 1, 1));
    }

    @Test
    @DisplayName("inicioEjercicio explícito se respeta (ejercicio fiscal no calendario)")
    void inicio_explicito() {
        var f = new BalanceGeneralFilter(
                LocalDate.of(2026, 12, 31),
                LocalDate.of(2026, 7, 1),  // ejercicio fiscal julio-junio
                false);
        assertThat(f.inicioEjercicioResuelto()).isEqualTo(LocalDate.of(2026, 7, 1));
    }

    @Test
    @DisplayName("fechaCorte null → rechazado")
    void fecha_null_rechazada() {
        assertThatThrownBy(() -> new BalanceGeneralFilter(null, null, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fechaCorte");
    }

    @Test
    @DisplayName("inicioEjercicio posterior a fechaCorte → rechazado")
    void inicio_posterior_a_corte_rechazado() {
        assertThatThrownBy(() -> new BalanceGeneralFilter(
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 7, 1),
                false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("posterior");
    }

    @Test
    @DisplayName("incluirCeros se almacena")
    void incluir_ceros_se_almacena() {
        var f = new BalanceGeneralFilter(LocalDate.now(), null, true);
        assertThat(f.incluirCeros()).isTrue();
    }
}
