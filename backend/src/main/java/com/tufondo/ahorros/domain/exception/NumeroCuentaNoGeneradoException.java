package com.tufondo.ahorros.domain.exception;

public class NumeroCuentaNoGeneradoException extends RuntimeException {
    
    public NumeroCuentaNoGeneradoException() {
        super("No se pudo generar número de cuenta único después de 5 intentos");
    }
    
    public NumeroCuentaNoGeneradoException(String message) {
        super(message);
    }
}
