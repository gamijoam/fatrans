// com/tufondo/creditos/domain/repository/PlanAmortizacionRepository.java
package com.tufondo.creditos.domain.repository;

import com.tufondo.creditos.domain.model.PlanAmortizacion;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface para PlanAmortizacion.
 */
public interface PlanAmortizacionRepository {
    
    /**
     * Guarda un plan de amortización.
     */
    PlanAmortizacion guardar(PlanAmortizacion plan);
    
    /**
     * Busca plan por ID.
     */
    Optional<PlanAmortizacion> buscarPorId(UUID id);
    
    /**
     * Busca plan por ID de solicitud.
     */
    Optional<PlanAmortizacion> buscarPorSolicitudId(UUID solicitudId);
    
    /**
     * Verifica si existe plan para la solicitud.
     */
    boolean existePorSolicitudId(UUID solicitudId);
}
