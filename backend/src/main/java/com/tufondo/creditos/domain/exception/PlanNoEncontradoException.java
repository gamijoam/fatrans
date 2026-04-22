// com/tufondo/creditos/domain/exception/PlanNoEncontradoException.java
package com.tufondo.creditos.domain.exception;

/**
 * Excepción cuando no se encuentra un plan de amortización.
 */
public class PlanNoEncontradoException extends RuntimeException {
    
    public PlanNoEncontradoException(String planId) {
        super(String.format("Plan de amortización no encontrado: %s", planId));
    }
}