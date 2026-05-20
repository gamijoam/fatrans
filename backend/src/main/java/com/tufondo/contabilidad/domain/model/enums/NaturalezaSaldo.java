package com.tufondo.contabilidad.domain.model.enums;

import java.math.BigDecimal;

/**
 * Naturaleza del saldo de una cuenta contable.
 *
 * <p>Determina qué lado del asiento (DEBE/HABER) AUMENTA el saldo de la cuenta
 * y cuál lo DISMINUYE. Es el concepto base de partida doble:</p>
 *
 * <pre>
 *   Cuenta DEUDORA (ej. Caja, Cuentas por Cobrar, Gastos):
 *     DEBE  → aumenta saldo
 *     HABER → disminuye saldo
 *
 *   Cuenta ACREEDORA (ej. Depósitos, Patrimonio, Ingresos):
 *     DEBE  → disminuye saldo
 *     HABER → aumenta saldo
 * </pre>
 *
 * <p>El campo {@code signoSaldo()} devuelve +1 / -1 según la convención: el
 * saldo final de una cuenta es <em>(sumas del DEBE − sumas del HABER) × signoSaldo</em>.
 * Esto permite calcular el saldo "presentable" (siempre positivo cuando la
 * cuenta opera normalmente) sin acoplarse a la implementación de cálculo.</p>
 */
public enum NaturalezaSaldo {
    /** Aumenta en el DEBE, disminuye en el HABER. */
    DEUDORA(1),
    /** Aumenta en el HABER, disminuye en el DEBE. */
    ACREEDORA(-1);

    private final int signo;

    NaturalezaSaldo(int signo) {
        this.signo = signo;
    }

    /**
     * Devuelve +1 para deudoras, -1 para acreedoras.
     *
     * <p>Uso: dado <em>movimiento = sumaDebe − sumaHaber</em>, el saldo
     * presentable de la cuenta es <em>movimiento × signoSaldo()</em>, que
     * será positivo cuando la cuenta opera en condiciones normales.</p>
     */
    public int signoSaldo() {
        return signo;
    }

    /**
     * Calcula el saldo presentable de una cuenta dado el total de
     * movimientos al DEBE y al HABER. Conveniencia.
     *
     * @param totalDebe suma de todas las partidas al DEBE (no negativa)
     * @param totalHaber suma de todas las partidas al HABER (no negativa)
     * @return saldo presentable; positivo si la cuenta tiene saldo "normal"
     */
    public BigDecimal calcularSaldo(BigDecimal totalDebe, BigDecimal totalHaber) {
        BigDecimal movimiento = totalDebe.subtract(totalHaber);
        return this == DEUDORA ? movimiento : movimiento.negate();
    }
}
