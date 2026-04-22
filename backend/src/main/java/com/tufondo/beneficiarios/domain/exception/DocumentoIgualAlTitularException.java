// com/tufondo/beneficiarios/domain/exception/DocumentoIgualAlTitularException.java
package com.tufondo.beneficiarios.domain.exception;

/**
 * Excepción lanzada cuando el documento del beneficiario es igual al del socio titular.
 */
public class DocumentoIgualAlTitularException extends RuntimeException {
    public DocumentoIgualAlTitularException() {
        super("El documento del beneficiario no puede ser igual al del socio titular");
    }
}