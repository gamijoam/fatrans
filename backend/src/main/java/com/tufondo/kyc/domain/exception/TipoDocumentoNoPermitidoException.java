// com.tufondo.kyc.domain.exception.TipoDocumentoNoPermitidoException
package com.tufondo.kyc.domain.exception;

/**
 * Excepcion cuando el tipo de documento no esta permitido para el nivel KYC.
 */
public class TipoDocumentoNoPermitidoException extends KYCException {

    public TipoDocumentoNoPermitidoException(String message) {
        super("KYC_009", message);
    }
}