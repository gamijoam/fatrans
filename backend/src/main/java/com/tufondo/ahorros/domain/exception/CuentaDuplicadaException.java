// com/tufondo/ahorros/domain/exception/CuentaDuplicadaException.java
package com.tufondo.ahorros.domain.exception;

import java.util.UUID;

/**
 * Excepción cuando un socio ya tiene una cuenta del mismo tipo.
 * RN-001: Un socio solo puede tener una cuenta por tipo.
 */
public class CuentaDuplicadaException extends RuntimeException {
    public CuentaDuplicadaException(UUID socioId, String tipoCuenta) {
        super("El socio " + socioId + " ya tiene una cuenta de tipo " + tipoCuenta);
    }
}