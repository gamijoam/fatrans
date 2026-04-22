// com/tufondo/creditos/domain/exception/EstadoCreditoInvalidoException.java
package com.tufondo.creditos.domain.exception;

import com.tufondo.creditos.domain.model.enums.EstadoSolicitud;

/**
 * Excepción cuando el estado del crédito no permite la operación.
 */
public class EstadoCreditoInvalidoException extends RuntimeException {
    
    public EstadoCreditoInvalidoException(String numeroSolicitud, EstadoSolicitud estadoActual, String operacion) {
        super(String.format("La solicitud %s está en estado %s, no permite: %s", 
            numeroSolicitud, estadoActual, operacion));
    }
}
