// com.tufondo.kyc.domain.exception.DocumentoNoEliminableException
package com.tufondo.kyc.domain.exception;

/**
 * Excepcion cuando un documento no puede ser eliminado.
 */
public class DocumentoNoEliminableException extends KYCException {

    public DocumentoNoEliminableException(String message) {
        super("KYC_007", message);
    }
}