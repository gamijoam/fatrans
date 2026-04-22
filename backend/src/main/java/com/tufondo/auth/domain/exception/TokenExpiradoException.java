package com.tufondo.auth.domain.exception;

public class TokenExpiradoException extends RuntimeException {
    
    public TokenExpiradoException(String mensaje) {
        super(mensaje);
    }
}
