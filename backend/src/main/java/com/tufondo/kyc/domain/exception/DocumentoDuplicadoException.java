// com.tufondo.kyc.domain.exception.DocumentoDuplicadoException
package com.tufondo.kyc.domain.exception;

/**
 * Excepcion cuando ya existe un documento del mismo tipo.
 */
public class DocumentoDuplicadoException extends KYCException {

    public DocumentoDuplicadoException(String message) {
        super("KYC_012", message);
    }
}