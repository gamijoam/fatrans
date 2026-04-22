// 📁 com/tufondo/socios/domain/exception/FormatoCorreoInvalidoException.java
package com.tufondo.socios.domain.exception;

public class FormatoCorreoInvalidoException extends RuntimeException {
    public FormatoCorreoInvalidoException(String correo) {
        super("El formato del correo electrónico es inválido: " + correo);
    }
}
