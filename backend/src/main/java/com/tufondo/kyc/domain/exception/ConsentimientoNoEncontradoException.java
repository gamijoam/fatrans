// com.tufondo.kyc.domain.exception.ConsentimientoNoEncontradoException
package com.tufondo.kyc.domain.exception;

/**
 * Excepción lanzada cuando no se encuentra un consentimiento.
 */
public class ConsentimientoNoEncontradoException extends KYCException {

    public ConsentimientoNoEncontradoException() {
        super("KYC_013", "No se encontró consentimiento activo para el socio");
    }

    public ConsentimientoNoEncontradoException(String socioId) {
        super("KYC_013", "No se encontró consentimiento activo para el socio: " + socioId);
    }
}