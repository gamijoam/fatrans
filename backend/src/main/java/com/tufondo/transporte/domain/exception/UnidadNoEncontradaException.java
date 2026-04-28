package com.tufondo.transporte.domain.exception;

import java.util.UUID;

public class UnidadNoEncontradaException extends RuntimeException {
    public UnidadNoEncontradaException(UUID id) {
        super(String.format("No se encontró la unidad de transporte con ID: %s", id));
    }
}
