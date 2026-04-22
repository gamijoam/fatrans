// com.tufondo.kyc.domain.exception.AccesoNoAutorizadoException
package com.tufondo.kyc.domain.exception;

/**
 * Excepcion cuando un usuario no tiene acceso a un recurso.
 */
public class AccesoNoAutorizadoException extends KYCException {

    public AccesoNoAutorizadoException(String message) {
        super("KYC_004", message);
    }
}