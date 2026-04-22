// 📁 com/tufondo/socios/domain/exception/CorreoYaRegistradoException.java
package com.tufondo.socios.domain.exception;

public class CorreoYaRegistradoException extends RuntimeException {
    public CorreoYaRegistradoException(String correo) {
        super("Ya existe un socio registrado con el correo electrónico: " + correo);
    }
}
