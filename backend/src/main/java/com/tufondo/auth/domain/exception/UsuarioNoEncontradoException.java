package com.tufondo.auth.domain.exception;

public class UsuarioNoEncontradoException extends RuntimeException {
    
    public UsuarioNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
