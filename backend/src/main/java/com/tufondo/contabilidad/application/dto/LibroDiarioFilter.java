package com.tufondo.contabilidad.application.dto;

import java.time.LocalDate;

/**
 * Filtros para la generación del Libro Diario (sub-issue #269).
 *
 * <p>El rango {@code desde-hasta} es obligatorio y debe ser ≤ 1 año fiscal
 * para evitar reportes gigantes que comprometan memoria/timeout. SUDECA
 * típicamente exige el Libro Diario mensual o anual — pedirlo por décadas
 * no tiene sentido operativo.</p>
 *
 * @param desde            inicio del período (inclusive)
 * @param hasta            fin del período (inclusive)
 * @param incluirAnulados  si {@code true} (default), incluye asientos
 *                         ANULADOS marcados visualmente. Si {@code false},
 *                         solo REGISTRADOS. SUDECA exige normalmente el
 *                         "todo" con la marca, no la exclusión.
 */
public record LibroDiarioFilter(
        LocalDate desde,
        LocalDate hasta,
        boolean incluirAnulados
) {
    public static final int RANGO_MAX_DIAS = 366; // 1 año bisiesto

    /** Constructor con defaults. */
    public LibroDiarioFilter {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("desde y hasta son obligatorios");
        }
        if (hasta.isBefore(desde)) {
            throw new IllegalArgumentException("hasta no puede ser anterior a desde");
        }
        long dias = java.time.temporal.ChronoUnit.DAYS.between(desde, hasta);
        if (dias > RANGO_MAX_DIAS) {
            throw new IllegalArgumentException(String.format(
                    "rango de %d días excede el máximo de %d (1 año fiscal). " +
                            "Divida la consulta en períodos menores.",
                    dias, RANGO_MAX_DIAS));
        }
    }

    /** Constructor por defecto con incluirAnulados=true. */
    public static LibroDiarioFilter de(LocalDate desde, LocalDate hasta) {
        return new LibroDiarioFilter(desde, hasta, true);
    }
}
