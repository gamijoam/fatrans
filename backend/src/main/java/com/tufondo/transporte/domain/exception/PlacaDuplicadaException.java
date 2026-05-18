package com.tufondo.transporte.domain.exception;

public class PlacaDuplicadaException extends RuntimeException {
    public PlacaDuplicadaException(String placa) {
        super(String.format("Ya existe una unidad registrada con la placa: %s", placa));
    }
}
