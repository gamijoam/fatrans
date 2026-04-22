// com/tufondo/ahorros/domain/exception/TasaInvalidaException.java
package com.tufondo.ahorros.domain.exception;

import java.math.BigDecimal;

/**
 * Excepción cuando la tasa está fuera del rango válido.
 * RN-010: tasaAplicada debe estar en rango 0.0001 - 1.0 (CRÍTICO overflow).
 */
public class TasaInvalidaException extends RuntimeException {
    public TasaInvalidaException(BigDecimal tasa) {
        super("Tasa inválida: " + tasa + ". Debe estar entre 0.0001 y 1.0");
    }
}