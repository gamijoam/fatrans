// com/tufondo/creditos/domain/exception/MontoInsuficienteException.java
package com.tufondo.creditos.domain.exception;

import java.math.BigDecimal;

/**
 * Excepción cuando el monto de pago es insuficiente.
 */
public class MontoInsuficienteException extends RuntimeException {
    
    public MontoInsuficienteException(BigDecimal montoRecibido, BigDecimal montoRequerido) {
        super(String.format("Monto insuficiente. Recibido: %s, Requerido: %s", montoRecibido, montoRequerido));
    }
}
