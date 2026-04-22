// com.tufondo.kyc.domain.exception.DocumentoFormatoInvalidoException
package com.tufondo.kyc.domain.exception;

/**
 * Excepcion cuando el formato de un documento es invalido.
 */
public class DocumentoFormatoInvalidoException extends KYCException {

    public DocumentoFormatoInvalidoException(String message) {
        super("KYC_002", message);
    }
}