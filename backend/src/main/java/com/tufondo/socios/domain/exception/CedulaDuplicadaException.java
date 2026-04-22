// 📁 com/tufondo/socios/domain/exception/CedulaDuplicadaException.java
package com.tufondo.socios.domain.exception;

public class CedulaDuplicadaException extends RuntimeException {
    
    public CedulaDuplicadaException(String cedula) {
        super("Ya existe una solicitud o socio con la cédula: " + cedula);
    }
}