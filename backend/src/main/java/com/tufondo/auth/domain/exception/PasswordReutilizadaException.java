package com.tufondo.auth.domain.exception;

public class PasswordReutilizadaException extends RuntimeException {
    
    public PasswordReutilizadaException(String message) {
        super(message);
    }
}