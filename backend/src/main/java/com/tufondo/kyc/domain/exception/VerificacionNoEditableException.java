// com.tufondo.kyc.domain.exception.VerificacionNoEditableException
package com.tufondo.kyc.domain.exception;

/**
 * Excepcion cuando una verificacion no puede ser editada.
 */
public class VerificacionNoEditableException extends KYCException {

    public VerificacionNoEditableException(String message) {
        super("KYC_006", message);
    }
}