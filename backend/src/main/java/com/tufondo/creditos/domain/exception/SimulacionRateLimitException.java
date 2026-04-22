// com/tufondo/creditos/domain/exception/SimulacionRateLimitException.java
package com.tufondo.creditos.domain.exception;

/**
 * Excepción cuando se excede el rate limit del simulador de créditos.
 */
public class SimulacionRateLimitException extends RuntimeException {
    
    private final int retryAfterSeconds;
    
    public SimulacionRateLimitException(int retryAfterSeconds) {
        super(String.format("Rate limit excedido para simulación. Retry after %d seconds", retryAfterSeconds));
        this.retryAfterSeconds = retryAfterSeconds;
    }
    
    public int getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}