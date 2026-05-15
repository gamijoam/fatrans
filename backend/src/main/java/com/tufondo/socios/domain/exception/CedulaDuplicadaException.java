// 📁 com/tufondo/socios/domain/exception/CedulaDuplicadaException.java
package com.tufondo.socios.domain.exception;

/**
 * Anti-enumeración (audit 2026-05-15 #4): el mensaje que se devuelve al cliente
 * NO incluye el valor de la cédula — evita confirmar al atacante si una cédula
 * está registrada en el sistema. El valor sigue accesible internamente vía
 * {@link #getCedula()} para auditoría/logs server-side.
 */
public class CedulaDuplicadaException extends RuntimeException {

    private static final String MENSAJE_GENERICO =
            "No es posible procesar la solicitud con los datos proporcionados.";

    private final String cedula;

    public CedulaDuplicadaException(String cedula) {
        super(MENSAJE_GENERICO);
        this.cedula = cedula;
    }

    public String getCedula() {
        return cedula;
    }
}