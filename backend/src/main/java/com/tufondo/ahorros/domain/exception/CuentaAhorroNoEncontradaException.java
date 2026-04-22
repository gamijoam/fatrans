// com/tufondo/ahorros/domain/exception/CuentaAhorroNoEncontradaException.java
package com.tufondo.ahorros.domain.exception;

/**
 * Excepción cuando una cuenta de ahorro no existe.
 */
public class CuentaAhorroNoEncontradaException extends RuntimeException {
    public CuentaAhorroNoEncontradaException(String numeroCuenta) {
        super("No existe cuenta con número " + numeroCuenta);
    }
}