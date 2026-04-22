package com.tufondo.auth.domain.exception;

/**
 * Excepción lanzada cuando la nueva contraseña no cumple los requisitos de seguridad.
 */
public class PasswordNoCumpleRequisitosException extends RuntimeException {
    
    public PasswordNoCumpleRequisitosException(String mensaje) {
        super(mensaje);
    }
}
