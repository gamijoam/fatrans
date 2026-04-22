// com/tufondo/creditos/domain/exception/CreditoNoEncontradoException.java
package com.tufondo.creditos.domain.exception;

/**
 * Excepción cuando no se encuentra una solicitud de crédito.
 */
public class CreditoNoEncontradoException extends RuntimeException {
    
    public CreditoNoEncontradoException(String numeroSolicitud) {
        super(String.format("Solicitud de crédito no encontrada: %s", numeroSolicitud));
    }
    
    public CreditoNoEncontradoException(Long tipoCreditoId) {
        super(String.format("Tipo de crédito no encontrado: %d", tipoCreditoId));
    }
}
