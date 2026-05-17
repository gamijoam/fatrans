// Puerto compartido para consultar datos del módulo Socios
package com.tufondo.core.port;

import java.util.UUID;

/**
 * Puerto para consultar datos del módulo Socios desde otros módulos
 * (ej. documentospdf) sin acoplarse al modelo de dominio interno.
 *
 * <p><strong>Nota arquitectónica (issue #199):</strong> previamente este
 * puerto tenía un método {@code obtenerSocioIdPorCuenta(UUID)} que se
 * eliminó porque (a) era código muerto — ningún consumidor lo llamaba
 * y todos los flujos resuelven cuentaId→socioId vía
 * {@code CuentaQueryPort.obtenerDatosCuenta()} que ya retorna el
 * socioId; (b) implementarlo aquí violaría hexagonal — el módulo
 * {@code socios} no debe depender del módulo {@code ahorros}. Si en el
 * futuro algún flujo necesita la relación cuenta→socio fuera de
 * Cuenta, créese un puerto en {@code core.port} que el módulo
 * {@code ahorros} implemente.</p>
 */
public interface SocioQueryPort {

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
     * @return Mapa con datos del socio o {@code null} si no existe
     */
    java.util.Map<String, Object> obtenerDatosSocioParaPdf(UUID socioId);
}
