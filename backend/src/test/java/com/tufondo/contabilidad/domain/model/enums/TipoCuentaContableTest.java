package com.tufondo.contabilidad.domain.model.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class TipoCuentaContableTest {

    @Test
    @DisplayName("Naturaleza natural ACTIVO → DEUDORA")
    void activo_es_deudora() {
        assertThat(TipoCuentaContable.ACTIVO.naturalezaNatural())
                .isEqualTo(NaturalezaSaldo.DEUDORA);
    }

    @Test
    @DisplayName("Naturaleza natural EGRESO → DEUDORA")
    void egreso_es_deudora() {
        assertThat(TipoCuentaContable.EGRESO.naturalezaNatural())
                .isEqualTo(NaturalezaSaldo.DEUDORA);
    }

    @Test
    @DisplayName("Naturaleza natural PASIVO → ACREEDORA")
    void pasivo_es_acreedora() {
        assertThat(TipoCuentaContable.PASIVO.naturalezaNatural())
                .isEqualTo(NaturalezaSaldo.ACREEDORA);
    }

    @Test
    @DisplayName("Naturaleza natural PATRIMONIO → ACREEDORA")
    void patrimonio_es_acreedora() {
        assertThat(TipoCuentaContable.PATRIMONIO.naturalezaNatural())
                .isEqualTo(NaturalezaSaldo.ACREEDORA);
    }

    @Test
    @DisplayName("Naturaleza natural INGRESO → ACREEDORA")
    void ingreso_es_acreedora() {
        assertThat(TipoCuentaContable.INGRESO.naturalezaNatural())
                .isEqualTo(NaturalezaSaldo.ACREEDORA);
    }

    @ParameterizedTest
    @EnumSource(TipoCuentaContable.class)
    @DisplayName("Todos los tipos tienen una naturaleza no nula")
    void todos_los_tipos_tienen_naturaleza_definida(TipoCuentaContable tipo) {
        assertThat(tipo.naturalezaNatural()).isNotNull();
    }
}
