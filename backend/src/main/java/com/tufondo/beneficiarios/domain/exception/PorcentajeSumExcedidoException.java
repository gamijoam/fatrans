// com/tufondo/beneficiarios/domain/exception/PorcentajeSumExcedidoException.java
package com.tufondo.beneficiarios.domain.exception;

import java.math.BigDecimal;

/**
 * Excepción lanzada cuando la suma de porcentajes excedería 100%.
 */
public class PorcentajeSumExcedidoException extends RuntimeException {
    public PorcentajeSumExcedidoException(BigDecimal sumaActual, BigDecimal nuevoPorcentaje) {
        super(String.format("La suma de porcentajes excedería 100%%. Suma actual: %.2f, nuevo porcentaje: %.2f",
                sumaActual, nuevoPorcentaje));
    }
}