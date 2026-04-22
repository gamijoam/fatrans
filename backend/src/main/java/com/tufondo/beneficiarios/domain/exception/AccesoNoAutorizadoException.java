// com/tufondo/beneficiarios/domain/exception/AccesoNoAutorizadoException.java
package com.tufondo.beneficiarios.domain.exception;

/**
 * Excepción lanzada cuando un usuario intenta acceder a recursos no autorizados.
 */
public class AccesoNoAutorizadoException extends RuntimeException {

    public AccesoNoAutorizadoException() {
        super("Acceso no autorizado");
    }

    public AccesoNoAutorizadoException(String mensaje) {
        super(mensaje);
    }
}