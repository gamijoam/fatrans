// com/tufondo/creditos/domain/exception/PagoDuplicadoException.java
package com.tufondo.creditos.domain.exception;

/**
 * Excepción cuando se intenta pagar con la misma referencia (idempotency check).
 */
public class PagoDuplicadoException extends RuntimeException {
    
    public PagoDuplicadoException(String referencia) {
        super(String.format("Ya existe un pago con referencia: %s", referencia));
    }
}
