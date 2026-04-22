// com/tufondo/ahorros/domain/exception/AccesoCuentaAjenaException.java
package com.tufondo.ahorros.domain.exception;

/**
 * Excepción para IDOR check fallido.
 * El socio no tiene acceso a la cuenta.
 */
public class AccesoCuentaAjenaException extends RuntimeException {
    public AccesoCuentaAjenaException() {
        super("No tiene permiso para acceder a esta cuenta");
    }
}