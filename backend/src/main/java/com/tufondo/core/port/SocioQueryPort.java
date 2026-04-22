// Puerto compartido para consultar datos del módulo Socios
package com.tufondo.core.port;

import java.util.UUID;

/**
 * Puerto para consultar datos del módulo Socios.
 */
public interface SocioQueryPort {

    /**
     * Obtiene el ID del socio asociado a una cuenta.
     *
     * @param cuentaId ID de la cuenta
     * @return UUID del socio
     */
    UUID obtenerSocioIdPorCuenta(UUID cuentaId);

    /**
     * Verifica si un socio existe.
     *
     * @param socioId ID del socio
     * @return true si existe
     */
    boolean existeSocio(UUID socioId);

    /**
     * Obtiene datos públicos del socio para el PDF.
     *
     * @param socioId ID del socio
     * @return Mapa con datos del socio
     */
    java.util.Map<String, Object> obtenerDatosSocioParaPdf(UUID socioId);
}
