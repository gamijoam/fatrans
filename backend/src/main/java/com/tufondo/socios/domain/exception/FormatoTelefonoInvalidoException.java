// 📁 com/tufondo/socios/domain/exception/FormatoTelefonoInvalidoException.java
package com.tufondo.socios.domain.exception;

public class FormatoTelefonoInvalidoException extends RuntimeException {
    public FormatoTelefonoInvalidoException(String telefono) {
        super("El formato del número de teléfono es inválido: " + telefono);
    }
}
