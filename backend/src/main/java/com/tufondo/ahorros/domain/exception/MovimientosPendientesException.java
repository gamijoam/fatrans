package com.tufondo.ahorros.domain.exception;

import java.util.UUID;

public class MovimientosPendientesException extends RuntimeException {

    private final UUID cuentaId;

    public MovimientosPendientesException(UUID cuentaId) {
        super("La cuenta " + cuentaId + " tiene movimientos pendientes");
        this.cuentaId = cuentaId;
    }

    public MovimientosPendientesException(String message) {
        super(message);
        this.cuentaId = null;
    }

    public UUID getCuentaId() {
        return cuentaId;
    }
}
