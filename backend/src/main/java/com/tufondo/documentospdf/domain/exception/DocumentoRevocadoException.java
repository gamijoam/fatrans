// com.tufondo.documentospdf.domain.exception.DocumentoRevocadoException
package com.tufondo.documentospdf.domain.exception;

/**
 * Excepción lanzada cuando el documento ha sido revocado.
 * Código: DOC_003
 */
public class DocumentoRevocadoException extends RuntimeException {
    private static final String CODIGO = "DOC_003";

    public DocumentoRevocadoException(String mensaje) {
        super(mensaje);
    }

    public String getCodigo() {
        return CODIGO;
    }
}
