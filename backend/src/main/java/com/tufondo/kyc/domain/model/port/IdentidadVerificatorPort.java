// com.tufondo.kyc.domain.model.port.IdentidadVerificatorPort
package com.tufondo.kyc.domain.model.port;

import java.util.UUID;

/**
 * Puerto para verificacion de identidad con fuentes externas.
 * Implementaciones: LocalIdentidadVerificatorAdapter (actual)
 *                   SaimeIdentidadVerificatorAdapter (futuro)
 */
public interface IdentidadVerificatorPort {

    record ResultadoVerificacion(
        boolean exitoso,
        boolean datosCoinciden,
        String numeroCedula,
        String nombres,
        String apellidos,
        String fechaNacimiento,
        String mensajeError,
        String fuenteVerificacion
    ) {}

    record DatosVerificacion(
        UUID socioId,
        String numeroCedula,
        String primerNombre,
        String primerApellido
    ) {}

    ResultadoVerificacion verificar(DatosVerificacion datos);

    boolean estaDisponible();
}