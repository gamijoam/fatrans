// com.tufondo.documentospdf.domain.exception.DocumentoExpiradoException
package com.tufondo.documentospdf.domain.exception;

/**
 * Excepción lanzada cuando el documento ha expirado.
 * Código: DOC_002
 */
public class DocumentoExpiradoException extends RuntimeException {
    private static final String CODIGO = "DOC_002";

    public DocumentoExpiradoException(String mensaje) {
        super(mensaje);
    }

    public String getCodigo() {
        return CODIGO;
    }
}
