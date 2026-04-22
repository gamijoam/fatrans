// com.tufondo.kyc.domain.exception.KYCException
package com.tufondo.kyc.domain.exception;

/**
 * Excepcion base para errores del modulo KYC.
 */
public class KYCException extends RuntimeException {

    private final String errorCode;

    public KYCException(String message) {
        super(message);
        this.errorCode = "KYC_000";
    }

    public KYCException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}