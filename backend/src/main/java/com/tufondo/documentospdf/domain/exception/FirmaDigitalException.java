// com.tufondo.documentospdf.domain.exception.FirmaDigitalException
package com.tufondo.documentospdf.domain.exception;

/**
 * Excepción lanzada cuando ocurre un error en la firma digital.
 * Código: DOC_005
 */
public class FirmaDigitalException extends RuntimeException {
    private static final String CODIGO = "DOC_005";

    public FirmaDigitalException(String mensaje) {
        super(mensaje);
    }

    public FirmaDigitalException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public String getCodigo() {
        return CODIGO;
    }
}
