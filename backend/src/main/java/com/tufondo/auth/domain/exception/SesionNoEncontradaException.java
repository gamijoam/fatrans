package com.tufondo.auth.domain.exception;

public class SesionNoEncontradaException extends RuntimeException {
    
    public SesionNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
