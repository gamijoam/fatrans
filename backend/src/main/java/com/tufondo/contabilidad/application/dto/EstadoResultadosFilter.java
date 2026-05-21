package com.tufondo.contabilidad.application.dto;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Filtros para el Estado de Resultados (sub-issue #271).
 *
 * <p>A diferencia del Balance General (foto a una fecha), el Estado de
 * Resultados es <strong>por rango</strong>: ingresos y egresos del período.</p>
 */
public record EstadoResultadosFilter(
        LocalDate desde,
        LocalDate hasta,
        boolean incluirCeros
) {
    public static final int RANGO_MAX_DIAS = 366; // 1 año bisiesto

    public EstadoResultadosFilter {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("desde y hasta son obligatorios");
        }
        if (hasta.isBefore(desde)) {
            throw new IllegalArgumentException("hasta no puede ser anterior a desde");
        }
        long dias = ChronoUnit.DAYS.between(desde, hasta);
        if (dias > RANGO_MAX_DIAS) {
            throw new IllegalArgumentException(String.format(
                    "rango de %d días excede el máximo de %d (1 año fiscal)",
                    dias, RANGO_MAX_DIAS));
        }
    }

    public static EstadoResultadosFilter de(LocalDate desde, LocalDate hasta) {
        return new EstadoResultadosFilter(desde, hasta, false);
    }
}
