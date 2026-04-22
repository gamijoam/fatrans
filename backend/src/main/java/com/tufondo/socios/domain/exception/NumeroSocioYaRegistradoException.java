// 📁 com/tufondo/socios/domain/exception/NumeroSocioYaRegistradoException.java
package com.tufondo.socios.domain.exception;

public class NumeroSocioYaRegistradoException extends RuntimeException {

    public NumeroSocioYaRegistradoException(String numeroSocio) {
        super("Ya existe un socio registrado con el número de socio: " + numeroSocio);
    }
}
