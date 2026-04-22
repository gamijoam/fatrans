package com.tufondo.auth.domain.exception;

public class CuentaDesactivadaException extends RuntimeException {
    
    public CuentaDesactivadaException() {
        super("La cuenta ha sido desactivada");
    }
}
