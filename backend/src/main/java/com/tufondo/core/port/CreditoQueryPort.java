// Puerto compartido para consultar datos del módulo Créditos
package com.tufondo.core.port;

import java.util.List;
import java.util.UUID;

/**
 * Puerto para consultar datos del módulo Créditos.
 */
public interface CreditoQueryPort {

    /**
     * Obtiene los datos de un crédito.
     *
     * @param creditoId ID del crédito
     * @return Mapa con datos del crédito
     */
    java.util.Map<String, Object> obtenerDatosCredito(UUID creditoId);

    /**
     * Obtiene la tabla de amortización de un crédito.
     *
     * @param creditoId ID del crédito
     * @return Lista de mapas con datos de cada cuota
     */
    List<java.util.Map<String, Object>> obtenerTablaAmortizacion(UUID creditoId);

    /**
     * Obtiene los datos de una solicitud de crédito.
     *
     * @param solicitudId ID de la solicitud
     * @return Mapa con datos de la solicitud
     */
    java.util.Map<String, Object> obtenerDatosSolicitud(UUID solicitudId);

    /**
     * Obtiene el socio ID asociado a un crédito.
     *
     * @param creditoId ID del crédito
     * @return UUID del socio
     */
    UUID obtenerSocioIdPorCredito(UUID creditoId);
}
