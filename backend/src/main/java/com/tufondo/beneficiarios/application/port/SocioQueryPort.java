// com/tufondo/beneficiarios/application/port/SocioQueryPort.java
package com.tufondo.beneficiarios.application.port;

import java.util.UUID;

/**
 * Puerto para consultar información del socio titular.
 * Esta interfaz permite desacoplar el módulo Beneficiarios del módulo Socios.
 */
public interface SocioQueryPort {

    /**
     * Verifica si existe un socio activo con el ID especificado.
     * @param socioId ID del socio
     * @return true si existe y está activo
     */
    boolean existsByIdAndActivoTrue(UUID socioId);

    /**
     * Obtiene el número de documento de un socio.
     * @param socioId ID del socio
     * @return número de documento o null si no existe
     */
    String getNumeroDocumentoById(UUID socioId);
}