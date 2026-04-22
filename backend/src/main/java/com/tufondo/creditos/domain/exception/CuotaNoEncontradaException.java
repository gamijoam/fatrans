// com/tufondo/creditos/domain/exception/CuotaNoEncontradaException.java
package com.tufondo.creditos.domain.exception;

import java.util.UUID;

/**
 * Excepción cuando no se encuentra una cuota.
 */
public class CuotaNoEncontradaException extends RuntimeException {
    
    public CuotaNoEncontradaException(UUID cuotaId) {
        super(String.format("Cuota no encontrada: %s", cuotaId));
    }
}
