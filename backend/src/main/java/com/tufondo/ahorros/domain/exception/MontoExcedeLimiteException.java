// com/tufondo/ahorros/domain/exception/MontoExcedeLimiteException.java
package com.tufondo.ahorros.domain.exception;

import java.math.BigDecimal;

/**
 * Excepción cuando el monto excede los límites regulatorios.
 * Depósito máx: 500,000 MXN
 * Retiro máx: 50,000 MXN/día
 */
public class MontoExcedeLimiteException extends RuntimeException {
    private final BigDecimal montoRecibido;
    private final BigDecimal limitePermitido;
    
    public MontoExcedeLimiteException(BigDecimal montoRecibido, BigDecimal limitePermitido, String operacion) {
        super("El monto de " + operacion + " excede el límite de " + limitePermitido + " MXN");
        this.montoRecibido = montoRecibido;
        this.limitePermitido = limitePermitido;
    }
    
    public BigDecimal getMontoRecibido() { return montoRecibido; }
    public BigDecimal getLimitePermitido() { return limitePermitido; }
}