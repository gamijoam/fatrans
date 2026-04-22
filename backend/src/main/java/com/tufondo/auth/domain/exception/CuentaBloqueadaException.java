package com.tufondo.auth.domain.exception;

public class CuentaBloqueadaException extends RuntimeException {
    
    public CuentaBloqueadaException() {
        super("La cuenta está bloqueada temporalmente. Intente más tarde.");
    }
}
