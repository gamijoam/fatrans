// Puerto compartido para consultar datos del módulo Beneficiarios
package com.tufondo.core.port;

import java.util.List;
import java.util.UUID;

/**
 * Puerto para consultar datos del módulo Beneficiarios.
 */
public interface BeneficiarioQueryPort {

    /**
     * Obtiene la lista de beneficiarios activos de un socio.
     *
     * @param socioId ID del socio
     * @return Lista de mapas con datos de beneficiarios
     */
    List<java.util.Map<String, Object>> obtenerBeneficiariosActivos(UUID socioId);
}
