// com/tufondo/creditos/domain/exception/AccesoNoAutorizadoException.java
package com.tufondo.creditos.domain.exception;

/**
 * Excepción para acceso no autorizado (IDOR).
 */
public class AccesoNoAutorizadoException extends RuntimeException {
    
    public AccesoNoAutorizadoException() {
        super("No tiene permiso para acceder a esta solicitud");
    }
    
    public AccesoNoAutorizadoException(String mensaje) {
        super(mensaje);
    }
}
