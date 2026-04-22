// 📁 com/tufondo/socios/domain/exception/NumeroDocumentoYaRegistradoException.java
package com.tufondo.socios.domain.exception;

public class NumeroDocumentoYaRegistradoException extends RuntimeException {

    public NumeroDocumentoYaRegistradoException(String numeroDocumento) {
        super("Ya existe un socio registrado con el número de documento: " + numeroDocumento);
    }

    public NumeroDocumentoYaRegistradoException(String numeroDocumento, String tipoDocumento) {
        super("Ya existe un socio registrado con " + tipoDocumento + " número: " + numeroDocumento);
    }
}
