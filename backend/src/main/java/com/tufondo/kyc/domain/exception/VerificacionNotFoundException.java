// com.tufondo.kyc.domain.exception.VerificacionNotFoundException
package com.tufondo.kyc.domain.exception;

import java.util.UUID;

/**
 * Excepcion cuando no se encuentra una verificacion KYC.
 */
public class VerificacionNotFoundException extends KYCException {

    public VerificacionNotFoundException(UUID verificacionId) {
        super("KYC_005", "No se encontro verificacion KYC con ID: " + verificacionId);
    }
}