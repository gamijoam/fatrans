// com.tufondo.kyc.domain.exception.PoliticaPrivacidadInvalidaException
package com.tufondo.kyc.domain.exception;

/**
 * Excepción lanzada cuando la versión de política de privacidad no es válida.
 */
public class PoliticaPrivacidadInvalidaException extends KYCException {

    public PoliticaPrivacidadInvalidaException() {
        super("KYC_012", "La versión de política de privacidad no es válida o está inactiva");
    }

    public PoliticaPrivacidadInvalidaException(String version) {
        super("KYC_012", "La versión de política de privacidad '" + version + "' no es válida o está inactiva");
    }
}