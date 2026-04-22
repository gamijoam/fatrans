// com/tufondo/ahorros/domain/exception/RendimientoYaAplicadoException.java
package com.tufondo.ahorros.domain.exception;

/**
 * Excepción cuando se intenta recalcular un rendimiento ya aplicado.
 * RN-012: Un periodo no puede ser recalculado si ya fue aplicado.
 */
public class RendimientoYaAplicadoException extends RuntimeException {
    public RendimientoYaAplicadoException(String periodoInicio, String periodoFin) {
        super("El periodo " + periodoInicio + " a " + periodoFin + " ya tiene un rendimiento aplicado");
    }
}