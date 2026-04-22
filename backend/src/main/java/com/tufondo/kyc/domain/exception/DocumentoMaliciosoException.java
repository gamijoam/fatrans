package com.tufondo.kyc.domain.exception;

public class DocumentoMaliciosoException extends KYCException {

    private static final String CODIGO = "KYC_017";
    private static final String MENSAJE_DEFAULT = "El archivo contiene malware y fue rechazado";

    public DocumentoMaliciosoException() {
        super(CODIGO, MENSAJE_DEFAULT);
    }

    public DocumentoMaliciosoException(String mensaje) {
        super(CODIGO, mensaje);
    }
}