package com.tufondo.socios.application.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests para issue #204: validación de mayoría de edad en registro.
 */
class MayorDeEdadValidatorTest {

    private MayorDeEdadValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new MayorDeEdadValidator();
        context = Mockito.mock(ConstraintValidatorContext.class);
        // Default: edadMinima = 18
        validator.initialize(annotation(18));
    }

    @Test
    @DisplayName("null se considera válido (delegar a @NotNull)")
    void null_es_valido() {
        assertThat(validator.isValid(null, context)).isTrue();
    }

    @Test
    @DisplayName("Persona con 18 años recién cumplidos es válida")
    void edad_exacta_18_es_valida() {
        LocalDate hace18Anios = LocalDate.now().minusYears(18);
        assertThat(validator.isValid(hace18Anios, context)).isTrue();
    }

    @Test
    @DisplayName("Persona con 18 años + 1 día es válida")
    void edad_18_y_un_dia_es_valida() {
        LocalDate fecha = LocalDate.now().minusYears(18).minusDays(1);
        assertThat(validator.isValid(fecha, context)).isTrue();
    }

    @Test
    @DisplayName("Persona con 17 años + 364 días NO es válida (todavía no cumplió 18)")
    void edad_17_y_364_dias_no_es_valida() {
        // Justo el día antes del cumpleaños 18: aún tiene 17 años cumplidos
        LocalDate fecha = LocalDate.now().minusYears(18).plusDays(1);
        assertThat(validator.isValid(fecha, context)).isFalse();
    }

    @Test
    @DisplayName("Persona de 17 años (un año antes) NO es válida")
    void edad_17_no_es_valida() {
        LocalDate hace17Anios = LocalDate.now().minusYears(17);
        assertThat(validator.isValid(hace17Anios, context)).isFalse();
    }

    @Test
    @DisplayName("Persona de 10 años NO es válida")
    void edad_10_no_es_valida() {
        LocalDate hace10Anios = LocalDate.now().minusYears(10);
        assertThat(validator.isValid(hace10Anios, context)).isFalse();
    }

    @Test
    @DisplayName("Fecha futura NO es válida")
    void fecha_futura_no_es_valida() {
        LocalDate manana = LocalDate.now().plusDays(1);
        assertThat(validator.isValid(manana, context)).isFalse();
    }

    @Test
    @DisplayName("Fecha igual a hoy NO es válida (recién nacido)")
    void fecha_hoy_no_es_valida() {
        assertThat(validator.isValid(LocalDate.now(), context)).isFalse();
    }

    @Test
    @DisplayName("Persona de 21 años con edadMinima=21 es válida")
    void edad_minima_configurable_21() {
        validator.initialize(annotation(21));
        LocalDate hace21Anios = LocalDate.now().minusYears(21);
        assertThat(validator.isValid(hace21Anios, context)).isTrue();
    }

    @Test
    @DisplayName("Persona de 20 años con edadMinima=21 NO es válida")
    void edad_minima_configurable_20_falla() {
        validator.initialize(annotation(21));
        LocalDate hace20Anios = LocalDate.now().minusYears(20);
        assertThat(validator.isValid(hace20Anios, context)).isFalse();
    }

    /**
     * Crea un mock de la anotación con la edad mínima configurada.
     */
    private MayorDeEdad annotation(int edadMinima) {
        MayorDeEdad ann = Mockito.mock(MayorDeEdad.class);
        Mockito.when(ann.edadMinima()).thenReturn(edadMinima);
        return ann;
    }
}
