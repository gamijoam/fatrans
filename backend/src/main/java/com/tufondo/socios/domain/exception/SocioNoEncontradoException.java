package com.tufondo.socios.domain.exception;

public class SocioNoEncontradoException extends RuntimeException {
    public SocioNoEncontradoException(Object id) {
        super("Socio no encontrado con ID: " + id);
    }
}
