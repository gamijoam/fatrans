package com.tufondo.auth.domain.exception;

public class CredencialesInvalidasException extends RuntimeException {
    
    public CredencialesInvalidasException() {
        super("Nombre de usuario o contraseña incorrectos");
    }

    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
