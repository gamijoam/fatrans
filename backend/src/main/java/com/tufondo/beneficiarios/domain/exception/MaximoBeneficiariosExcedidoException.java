// com/tufondo/beneficiarios/domain/exception/MaximoBeneficiariosExcedidoException.java
package com.tufondo.beneficiarios.domain.exception;

/**
 * Excepción lanzada cuando el socio ya tiene el máximo de 5 beneficiarios activos.
 */
public class MaximoBeneficiariosExcedidoException extends RuntimeException {
    public MaximoBeneficiariosExcedidoException() {
        super("El socio ya tiene el máximo de 5 beneficiarios activos");
    }
}