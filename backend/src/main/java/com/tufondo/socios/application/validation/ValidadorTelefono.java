package com.tufondo.socios.application.validation;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import com.tufondo.socios.domain.exception.FormatoTelefonoInvalidoException;

@Component
public class ValidadorTelefono {
    
    private static final Pattern PATRON_TELEFONO = Pattern.compile("^\\+?[0-9]{7,15}$");
    private static final Pattern PATRON_LIMPIO = Pattern.compile("^[0-9]{7,15}$");
    
    public boolean esValido(String telefono) {
        if (telefono == null || telefono.isBlank()) {
            return true;
        }
        String telefonoLimpio = telefono.replaceAll("[\\s\\-()]", "");
        if (telefonoLimpio.startsWith("+")) {
            return PATRON_TELEFONO.matcher(telefonoLimpio).matches();
        }
        return PATRON_LIMPIO.matcher(telefonoLimpio).matches();
    }

    public String validarYNormalizar(String telefono) {
        if (telefono == null || telefono.isBlank()) {
            return null;
        }
        String telefonoLimpio = telefono.replaceAll("[\\s\\-()]", "");
        if (!esValido(telefonoLimpio)) {
            throw new FormatoTelefonoInvalidoException(telefono);
        }
        return telefonoLimpio;
    }
}
