// com/tufondo/creditos/domain/exception/CuotaYaPagadaException.java
package com.tufondo.creditos.domain.exception;

/**
 * Excepción cuando se intenta pagar una cuota ya pagada (double-payment prevention).
 */
public class CuotaYaPagadaException extends RuntimeException {
    
    public CuotaYaPagadaException() {
        super("La cuota ya fue pagada. No se puede procesar doble cobro.");
    }
}
