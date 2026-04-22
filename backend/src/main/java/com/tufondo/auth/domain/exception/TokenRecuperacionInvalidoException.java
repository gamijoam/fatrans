package com.tufondo.auth.domain.exception;

/**
 * Excepción lanzada cuando el token de recuperación de contraseña es inválido o ha expirado.
 */
public class TokenRecuperacionInvalidoException extends RuntimeException {
    
    public TokenRecuperacionInvalidoException(String mensaje) {
        super(mensaje);
    }
}
