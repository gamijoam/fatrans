// com.tufondo.kyc.domain.exception.DocumentoExcedeTamanoException
package com.tufondo.kyc.domain.exception;

/**
 * Excepcion cuando un documento excede el tamano maximo.
 */
public class DocumentoExcedeTamanoException extends KYCException {

    public DocumentoExcedeTamanoException(String message) {
        super("KYC_002", message);
    }
}