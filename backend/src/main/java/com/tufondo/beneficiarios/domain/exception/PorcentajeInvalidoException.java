// com/tufondo/beneficiarios/domain/exception/PorcentajeInvalidoException.java
package com.tufondo.beneficiarios.domain.exception;

import java.math.BigDecimal;

/**
 * Excepción lanzada cuando el porcentaje está fuera de rango válido (0.01 - 100.00).
 */
public class PorcentajeInvalidoException extends RuntimeException {
    public PorcentajeInvalidoException(BigDecimal porcentaje) {
        super("Porcentaje inválido: " + porcentaje + ". Debe estar entre 0.01 y 100.00");
    }
}