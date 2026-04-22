package com.tufondo.socios.application.validation;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import com.tufondo.socios.domain.exception.FormatoCorreoInvalidoException;

@Component
public class ValidadorCorreoElectronico {

    private static final Pattern PATRON_CORREO = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    public boolean esValido(String correo) {
        if (correo == null || correo.isBlank()) {
            return true;
        }
        return PATRON_CORREO.matcher(correo.trim()).matches();
    }

    public String validarYNormalizar(String correo) {
        if (correo == null || correo.isBlank()) {
            return null;
        }
        String correoLimpio = correo.trim().toLowerCase();
        if (!esValido(correoLimpio)) {
            throw new FormatoCorreoInvalidoException(correo);
        }
        return correoLimpio;
    }
}
