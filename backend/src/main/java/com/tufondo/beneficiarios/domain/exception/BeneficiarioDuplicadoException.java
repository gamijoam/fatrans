// com/tufondo/beneficiarios/domain/exception/BeneficiarioDuplicadoException.java
package com.tufondo.beneficiarios.domain.exception;

/**
 * Excepción lanzada cuando ya existe un beneficiario activo con el mismo documento.
 */
public class BeneficiarioDuplicadoException extends RuntimeException {
    public BeneficiarioDuplicadoException(String numeroDocumento) {
        super("Ya existe un beneficiario activo con el documento: " + numeroDocumento);
    }
}