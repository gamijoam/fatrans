// com.tufondo.documentospdf.domain.exception.GeneracionPDFException
package com.tufondo.documentospdf.domain.exception;

/**
 * Excepción lanzada cuando ocurre un error al generar el PDF.
 * Código: DOC_004
 */
public class GeneracionPDFException extends RuntimeException {
    private static final String CODIGO = "DOC_004";

    public GeneracionPDFException(String mensaje) {
        super(mensaje);
    }

    public GeneracionPDFException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public String getCodigo() {
        return CODIGO;
    }
}
