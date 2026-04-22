package com.tufondo.auth.domain.exception;

/**
 * Excepción lanzada cuando un socio ya tiene un usuario vinculado.
 */
public class SocioYaTieneUsuarioException extends RuntimeException {
    
    public SocioYaTieneUsuarioException(String mensaje) {
        super(mensaje);
    }
}
