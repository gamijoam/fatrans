// com.tufondo.documentospdf.domain.exception.TipoDocumentoInvalidoException
package com.tufondo.documentospdf.domain.exception;

/**
 * Excepción lanzada cuando el tipo de documento no es válido o
 * falla una regla de negocio (ej: beneficiarios no suman 100%).
 * Código: DOC_008
 */
public class TipoDocumentoInvalidoException extends RuntimeException {
    private static final String CODIGO = "DOC_008";

    public TipoDocumentoInvalidoException(String mensaje) {
        super(mensaje);
    }

    public String getCodigo() {
        return CODIGO;
    }
}
