// com.tufondo.kyc.domain.model.port.DocumentValidatorPort
package com.tufondo.kyc.domain.model.port;

/**
 * Puerto para validacion de documentos.
 * Permite agregar OCR, deteccion de manipulacion, liveness, etc.
 */
public interface DocumentValidatorPort {

    record ResultadoValidacion(
        boolean valido,
        int scoreCalidad,
        String datosExtraidos,
        String[] erroresValidacion,
        boolean requiereRevisionManual
    ) {}

    ResultadoValidacion validar(byte[] archivoBytes, String nombreArchivo);

    boolean soportaTipoDocumento(String tipoDocumento);
}