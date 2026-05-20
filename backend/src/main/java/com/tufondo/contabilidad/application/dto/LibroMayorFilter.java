package com.tufondo.contabilidad.application.dto;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Filtros para la generación del Libro Mayor (sub-issue #270).
 *
 * @param desde                 inicio del período (inclusive)
 * @param hasta                 fin del período (inclusive)
 * @param codigoCuenta          si presente, solo se incluye esa cuenta;
 *                              si null/vacío, todas las cuentas hoja del plan
 * @param incluirSinMovimientos si {@code true}, incluye cuentas que no
 *                              tuvieron ningún movimiento en el período
 *                              (útil para auditoría "completa")
 * @param incluirTotalizadoras  si {@code true}, también muestra cuentas de
 *                              nivel 1-2 (las que NO aceptan movimientos
 *                              directos pero suman las de sus hijas)
 */
public record LibroMayorFilter(
        LocalDate desde,
        LocalDate hasta,
        String codigoCuenta,
        boolean incluirSinMovimientos,
        boolean incluirTotalizadoras
) {
    public static final int RANGO_MAX_DIAS = 366; // 1 año bisiesto

    public LibroMayorFilter {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("desde y hasta son obligatorios");
        }
        if (hasta.isBefore(desde)) {
            throw new IllegalArgumentException("hasta no puede ser anterior a desde");
        }
        long dias = ChronoUnit.DAYS.between(desde, hasta);
        if (dias > RANGO_MAX_DIAS) {
            throw new IllegalArgumentException(String.format(
                    "rango de %d días excede el máximo de %d (1 año fiscal). " +
                            "Divida la consulta en períodos menores.",
                    dias, RANGO_MAX_DIAS));
        }
        // codigoCuenta puede ser null o vacío — se interpreta como "todas".
    }

    /** Factory: todas las cuentas con movimientos, sin totalizadoras. */
    public static LibroMayorFilter completo(LocalDate desde, LocalDate hasta) {
        return new LibroMayorFilter(desde, hasta, null, false, false);
    }

    /** Factory: solo una cuenta específica. */
    public static LibroMayorFilter deCuenta(LocalDate desde, LocalDate hasta, String codigoCuenta) {
        return new LibroMayorFilter(desde, hasta, codigoCuenta, true, false);
    }

    /** {@code true} si se debe filtrar a una cuenta específica. */
    public boolean filtraPorCuenta() {
        return codigoCuenta != null && !codigoCuenta.isBlank();
    }
}
