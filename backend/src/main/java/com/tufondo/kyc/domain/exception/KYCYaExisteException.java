// com.tufondo.kyc.domain.exception.KYCYaExisteException
package com.tufondo.kyc.domain.exception;

/**
 * Excepcion cuando un socio ya tiene un proceso KYC activo.
 */
public class KYCYaExisteException extends KYCException {

    public KYCYaExisteException(String message) {
        super("KYC_001", message);
    }
}