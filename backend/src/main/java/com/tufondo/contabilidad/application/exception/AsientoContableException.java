package com.tufondo.contabilidad.application.exception;

/**
 * Excepción base del módulo de contabilidad. Se mapea a HTTP 422 desde el
 * GlobalExceptionHandler — el problema es de validación de negocio, no
 * técnico.
 */
public class AsientoContableException extends RuntimeException {
    public AsientoContableException(String message) {
        super(message);
    }
    public AsientoContableException(String message, Throwable cause) {
        super(message, cause);
    }
}
