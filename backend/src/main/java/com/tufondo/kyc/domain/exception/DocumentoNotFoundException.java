// com.tufondo.kyc.domain.exception.DocumentoNotFoundException
package com.tufondo.kyc.domain.exception;

import java.util.UUID;

/**
 * Excepcion cuando no se encuentra un documento.
 */
public class DocumentoNotFoundException extends KYCException {

    public DocumentoNotFoundException(UUID documentoId) {
        super("KYC_005", "No se encontro documento con ID: " + documentoId);
    }
}