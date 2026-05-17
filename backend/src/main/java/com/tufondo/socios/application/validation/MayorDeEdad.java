package com.tufondo.socios.application.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Valida que un {@link java.time.LocalDate} representa una fecha de nacimiento
 * de una persona con al menos {@link #edadMinima()} años cumplidos al día de hoy.
 *
 * <p>Bloqueante legal (issue #204): Venezuela exige mayoría de edad (≥18) para
 * operaciones financieras (LOPNNA y Sudeban). Permitir registro de menores
 * expone a la institución a multas regulatorias.</p>
 *
 * <p>Acepta {@code null}; combinar con {@code @NotNull} si el campo es requerido.</p>
 *
 * <pre>{@code
 *  @NotNull
 *  @MayorDeEdad
 *  private LocalDate fechaNacimiento;
 * }</pre>
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MayorDeEdadValidator.class)
public @interface MayorDeEdad {

    /**
     * Edad mínima requerida en años cumplidos. Default: 18.
     */
    int edadMinima() default 18;

    String message() default "Debes tener al menos {edadMinima} años para registrarte";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
