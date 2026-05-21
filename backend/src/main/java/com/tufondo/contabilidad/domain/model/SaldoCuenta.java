package com.tufondo.contabilidad.domain.model;

import com.tufondo.contabilidad.domain.model.enums.NaturalezaSaldo;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Saldo acumulado de una cuenta a una fecha de corte.
 *
 * <p>Es el resultado de sumar todas las partidas DEBE/HABER que tocaron la
 * cuenta hasta cierta fecha (típicamente excluyendo asientos ANULADOS). El
 * <strong>saldo neto</strong> con su signo depende de la naturaleza de la
 * cuenta — ver {@link NaturalezaSaldo#calcularSaldo(BigDecimal, BigDecimal)}.</p>
 *
 * <p>Sub-issue #270 (Libro Mayor). Se usa para calcular:</p>
 * <ul>
 *   <li>Saldo inicial al comienzo del período (sumando todo lo previo a {@code desde-1}).</li>
 *   <li>Saldo final al cierre del período (sumando inicial + movimientos del rango).</li>
 * </ul>
 *
 * @param totalDebe  suma de todas las partidas al DEBE
 * @param totalHaber suma de todas las partidas al HABER
 */
public record SaldoCuenta(
        BigDecimal totalDebe,
        BigDecimal totalHaber
) {
    public SaldoCuenta {
        Objects.requireNonNull(totalDebe, "totalDebe requerido");
        Objects.requireNonNull(totalHaber, "totalHaber requerido");
    }

    /** Saldo cero — útil cuando la cuenta no tiene movimientos. */
    public static SaldoCuenta cero() {
        return new SaldoCuenta(BigDecimal.ZERO, BigDecimal.ZERO);
    }

    /**
     * Calcula el saldo neto firmado según la naturaleza de la cuenta.
     * <ul>
     *   <li>DEUDORA: {@code debe - haber} (positivo = saldo deudor)</li>
     *   <li>ACREEDORA: {@code haber - debe} (positivo = saldo acreedor)</li>
     * </ul>
     */
    public BigDecimal saldoNeto(NaturalezaSaldo naturaleza) {
        return naturaleza.calcularSaldo(totalDebe, totalHaber);
    }

    /** Suma otro saldo al actual (útil para sumar inicial + movimientos del período). */
    public SaldoCuenta mas(SaldoCuenta otro) {
        return new SaldoCuenta(
                this.totalDebe.add(otro.totalDebe),
                this.totalHaber.add(otro.totalHaber));
    }
}
