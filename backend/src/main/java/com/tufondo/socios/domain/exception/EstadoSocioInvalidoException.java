// 📁 com/tufondo/socios/domain/exception/EstadoSocioInvalidoException.java
package com.tufondo.socios.domain.exception;

import com.tufondo.socios.domain.model.enums.EstadoSocio;

public class EstadoSocioInvalidoException extends RuntimeException {

    public EstadoSocioInvalidoException(String mensaje) {
        super(mensaje);
    }

    public EstadoSocioInvalidoException(Object id, EstadoSocio estado) {
        super("No se puede modificar el socio con ID " + id + " debido a que su estado es: " + estado);
    }

    public EstadoSocioInvalidoException(Object id, String accion) {
        super("No se puede " + accion + " el socio con ID " + id + " debido a su estado actual");
    }
}
