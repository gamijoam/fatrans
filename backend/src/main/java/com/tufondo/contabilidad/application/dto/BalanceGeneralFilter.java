package com.tufondo.contabilidad.application.dto;

import java.time.LocalDate;
import java.time.Month;

/**
 * Filtros para el Balance General (sub-issue #271).
 *
 * <p>El Balance es una <strong>foto a una fecha</strong> (no rango). El
 * "Excedente del Ejercicio" se calcula entre {@code inicioEjercicio} y
 * {@code fechaCorte} (ver D-008.5).</p>
 *
 * @param fechaCorte       fecha a la que se evalúa el balance (inclusive)
 * @param inicioEjercicio  inicio del ejercicio fiscal para calcular el Excedente.
 *                         Si null, default = {@code 1-enero del año de fechaCorte}.
 * @param incluirCeros     si {@code true}, muestra TODAS las cuentas aunque tengan saldo cero.
 */
public record BalanceGeneralFilter(
        LocalDate fechaCorte,
        LocalDate inicioEjercicio,
        boolean incluirCeros
) {

    public BalanceGeneralFilter {
        if (fechaCorte == null) {
            throw new IllegalArgumentException("fechaCorte es obligatoria");
        }
        if (inicioEjercicio != null && inicioEjercicio.isAfter(fechaCorte)) {
            throw new IllegalArgumentException(
                    "inicioEjercicio no puede ser posterior a fechaCorte");
        }
    }

    /** Factory default: ejercicio fiscal calendario (1-enero del año del corte). */
    public static BalanceGeneralFilter al(LocalDate fechaCorte) {
        return new BalanceGeneralFilter(fechaCorte, null, false);
    }

    /**
     * Inicio del ejercicio: si no se proveyó, default = 1-enero del año del corte.
     * Si Fatrans operara con ejercicio fiscal no calendario (ej. julio-junio),
     * el contador debe pasar {@code inicioEjercicio} explícito.
     */
    public LocalDate inicioEjercicioResuelto() {
        if (inicioEjercicio != null) return inicioEjercicio;
        return LocalDate.of(fechaCorte.getYear(), Month.JANUARY, 1);
    }
}
