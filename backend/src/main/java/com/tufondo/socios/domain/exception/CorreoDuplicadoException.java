// 📁 com/tufondo/socios/domain/exception/CorreoDuplicadoException.java
package com.tufondo.socios.domain.exception;

/**
 * Anti-enumeración (audit 2026-05-15 #4): el mensaje al cliente NO contiene el
 * correo — evita que un atacante use el endpoint público de registro para
 * descubrir qué correos están registrados. El valor real queda accesible vía
 * {@link #getCorreo()} para logs internos.
 */
public class CorreoDuplicadoException extends RuntimeException {

    private static final String MENSAJE_GENERICO =
            "No es posible procesar la solicitud con los datos proporcionados.";

    private final String correo;

    public CorreoDuplicadoException(String correo) {
        super(MENSAJE_GENERICO);
        this.correo = correo;
    }

    public String getCorreo() {
        return correo;
    }
}