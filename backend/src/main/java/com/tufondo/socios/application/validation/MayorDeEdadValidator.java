package com.tufondo.socios.application.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;

/**
 * Validator de {@link MayorDeEdad}. Calcula edad cumplida al día de hoy
 * (zona horaria del servidor) usando {@link Period#between}.
 *
 * <p>{@code null} se considera válido — el caller debe combinar con
 * {@code @NotNull} si la fecha es requerida.</p>
 */
public class MayorDeEdadValidator implements ConstraintValidator<MayorDeEdad, LocalDate> {

    private int edadMinima;

    @Override
    public void initialize(MayorDeEdad constraintAnnotation) {
        this.edadMinima = constraintAnnotation.edadMinima();
    }

    @Override
    public boolean isValid(LocalDate fechaNacimiento, ConstraintValidatorContext context) {
        if (fechaNacimiento == null) {
            // Delegar la validación de null a @NotNull. No validamos aquí para
            // permitir composición flexible.
            return true;
        }

        LocalDate hoy = LocalDate.now(ZoneId.systemDefault());

        // Si la fecha está en el futuro o es hoy mismo, no hay edad válida.
        if (!fechaNacimiento.isBefore(hoy)) {
            return false;
        }

        int edadCumplida = Period.between(fechaNacimiento, hoy).getYears();
        return edadCumplida >= edadMinima;
    }
}
