// 📁 com/tufondo/socios/domain/exception/FechaNacimientoInvalidaException.java
// ✅ CORRECTO — Sin cambios necesarios
package com.tufondo.socios.domain.exception;

public class FechaNacimientoInvalidaException extends RuntimeException {
    public FechaNacimientoInvalidaException() {
        super("La fecha de nacimiento no puede ser una fecha futura");
    }
}
