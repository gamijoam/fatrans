// com/tufondo/ahorros/domain/exception/MovimientoNoEncontradoException.java
package com.tufondo.ahorros.domain.exception;

/**
 * Excepción cuando un movimiento no existe.
 */
public class MovimientoNoEncontradoException extends RuntimeException {
    public MovimientoNoEncontradoException(String numeroOperacion) {
        super("No existe movimiento con número " + numeroOperacion);
    }
}