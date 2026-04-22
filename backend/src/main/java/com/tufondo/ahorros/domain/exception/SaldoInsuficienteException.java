// com/tufondo/ahorros/domain/exception/SaldoInsuficienteException.java
package com.tufondo.ahorros.domain.exception;

/**
 * Excepción cuando el saldo es insuficiente para una operación.
 * RN-003: saldoActual nunca negativo.
 */
public class SaldoInsuficienteException extends RuntimeException {
    public SaldoInsuficienteException(String numeroCuenta) {
        super("Saldo insuficiente en cuenta " + numeroCuenta);
    }
}