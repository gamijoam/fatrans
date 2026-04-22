// 📁 com/tufondo/socios/domain/exception/CorreoDuplicadoException.java
package com.tufondo.socios.domain.exception;

public class CorreoDuplicadoException extends RuntimeException {
    
    public CorreoDuplicadoException(String correo) {
        super("Ya existe una solicitud o socio con el correo: " + correo);
    }
}