// com/tufondo/creditos/domain/exception/CuotaSinSolicitudException.java
package com.tufondo.creditos.domain.exception;

/**
 * Excepción cuando una cuota no tiene una solicitud asociada.
 */
public class CuotaSinSolicitudException extends RuntimeException {
    
    public CuotaSinSolicitudException(String cuotaId) {
        super(String.format("No se encontró solicitud asociada a la cuota: %s", cuotaId));
    }
}