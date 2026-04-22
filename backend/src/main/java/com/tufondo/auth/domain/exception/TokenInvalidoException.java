package com.tufondo.auth.domain.exception;

public class TokenInvalidoException extends RuntimeException {
    
    public TokenInvalidoException(String mensaje) {
        super(mensaje);
    }
}
