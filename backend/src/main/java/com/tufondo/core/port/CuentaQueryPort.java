// Puerto compartido para consultar datos del módulo Ahorros (cuentas)
package com.tufondo.core.port;

import java.util.List;
import java.util.UUID;

/**
 * Puerto para consultar datos del módulo Ahorros (cuentas).
 */
public interface CuentaQueryPort {

    /**
     * Obtiene los datos de una cuenta para el estado de cuenta.
     *
     * @param cuentaId ID de la cuenta
     * @return Mapa con datos de la cuenta
     */
    java.util.Map<String, Object> obtenerDatosCuenta(UUID cuentaId);

    /**
     * Obtiene los movimientos de una cuenta para un periodo.
     *
     * @param cuentaId ID de la cuenta
     * @param anio año del estado de cuenta
     * @param mes mes del estado de cuenta
     * @return Lista de mapas con datos de movimientos
     */
    List<java.util.Map<String, Object>> obtenerMovimientos(UUID cuentaId, int anio, int mes);
}
