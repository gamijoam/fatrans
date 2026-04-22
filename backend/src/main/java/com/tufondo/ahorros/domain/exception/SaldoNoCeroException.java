// com/tufondo/ahorros/domain/exception/SaldoNoCeroException.java
package com.tufondo.ahorros.domain.exception;

import java.math.BigDecimal;

/**
 * Excepción cuando se intenta cerrar una cuenta con saldo diferente de cero.
 */
public class SaldoNoCeroException extends RuntimeException {
    public SaldoNoCeroException(BigDecimal saldoActual) {
        super("No se puede cerrar la cuenta. Saldo actual: " + saldoActual);
    }
}