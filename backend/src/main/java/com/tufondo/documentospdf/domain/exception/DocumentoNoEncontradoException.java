// com.tufondo.documentospdf.domain.exception.DocumentoNoEncontradoException
package com.tufondo.documentospdf.domain.exception;

/**
 * Excepción lanzada cuando un documento no existe.
 * Código: DOC_001
 */
public class DocumentoNoEncontradoException extends RuntimeException {
    private static final String CODIGO = "DOC_001";

    public DocumentoNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    public String getCodigo() {
        return CODIGO;
    }
}
