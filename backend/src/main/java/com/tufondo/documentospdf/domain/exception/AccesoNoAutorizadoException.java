// com.tufondo.documentospdf.domain.exception.AccesoNoAutorizadoException
package com.tufondo.documentospdf.domain.exception;

/**
 * Excepción lanzada cuando se viola la validación IDOR.
 * Código: DOC_007
 */
public class AccesoNoAutorizadoException extends RuntimeException {
    private static final String CODIGO = "DOC_007";

    public AccesoNoAutorizadoException(String mensaje) {
        super(mensaje);
    }

    public String getCodigo() {
        return CODIGO;
    }
}
