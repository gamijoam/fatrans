// com/tufondo/creditos/domain/exception/ColateralInsuficienteException.java
package com.tufondo.creditos.domain.exception;

import java.math.BigDecimal;

/**
 * Excepción cuando el colateral es insuficiente.
 */
public class ColateralInsuficienteException extends RuntimeException {
    
    public ColateralInsuficienteException(BigDecimal requerido, BigDecimal disponible) {
        super(String.format("Colateral insuficiente. Requerido: %s, Disponible: %s", requerido, disponible));
    }
}
