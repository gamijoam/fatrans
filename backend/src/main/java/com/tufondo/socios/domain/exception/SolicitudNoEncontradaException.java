// 📁 com/tufondo/socios/domain/exception/SolicitudNoEncontradaException.java
package com.tufondo.socios.domain.exception;

import java.util.UUID;

public class SolicitudNoEncontradaException extends RuntimeException {
    
    public SolicitudNoEncontradaException(UUID id) {
        super("Solicitud de registro no encontrada con ID: " + id);
    }
    
    public SolicitudNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}