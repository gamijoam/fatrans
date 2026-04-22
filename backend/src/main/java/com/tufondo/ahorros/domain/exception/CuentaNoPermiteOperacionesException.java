// com/tufondo/ahorros/domain/exception/CuentaNoPermiteOperacionesException.java
package com.tufondo.ahorros.domain.exception;

/**
 * Excepción cuando la cuenta no permite operaciones.
 * RN-005: Cuenta CERRADA no permite operaciones.
 */
public class CuentaNoPermiteOperacionesException extends RuntimeException {
    public CuentaNoPermiteOperacionesException(String numeroCuenta, String estado) {
        super("La cuenta " + numeroCuenta + " está en estado " + estado + " y no permite operaciones");
    }
}