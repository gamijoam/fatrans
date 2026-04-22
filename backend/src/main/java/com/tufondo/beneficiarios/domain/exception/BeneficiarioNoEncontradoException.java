// com/tufondo/beneficiarios/domain/exception/BeneficiarioNoEncontradoException.java
package com.tufondo.beneficiarios.domain.exception;

/**
 * Excepción lanzada cuando no se encuentra un beneficiario.
 */
public class BeneficiarioNoEncontradoException extends RuntimeException {
    public BeneficiarioNoEncontradoException(Object id) {
        super("Beneficiario no encontrado con ID: " + id);
    }
}