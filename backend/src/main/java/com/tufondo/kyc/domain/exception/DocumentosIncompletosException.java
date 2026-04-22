// com.tufondo.kyc.domain.exception.DocumentosIncompletosException
package com.tufondo.kyc.domain.exception;

/**
 * Excepcion cuando faltan documentos por subir.
 */
public class DocumentosIncompletosException extends KYCException {

    public DocumentosIncompletosException(String message) {
        super("KYC_003", message);
    }
}