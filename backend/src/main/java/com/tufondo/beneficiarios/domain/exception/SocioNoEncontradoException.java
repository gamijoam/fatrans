// com/tufondo/beneficiarios/domain/exception/SocioNoEncontradoException.java
package com.tufondo.beneficiarios.domain.exception;

/**
 * Excepción lanzada cuando no se encuentra un socio.
 */
public class SocioNoEncontradoException extends RuntimeException {
    public SocioNoEncontradoException(Object id) {
        super("Socio no encontrado con ID: " + id);
    }
}