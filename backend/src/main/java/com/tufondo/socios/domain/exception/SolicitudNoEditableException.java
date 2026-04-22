// 📁 com/tufondo/socios/domain/exception/SolicitudNoEditableException.java
package com.tufondo.socios.domain.exception;

public class SolicitudNoEditableException extends RuntimeException {
    
    public SolicitudNoEditableException(String mensaje) {
        super(mensaje);
    }
}