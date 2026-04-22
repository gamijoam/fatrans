// com/tufondo/creditos/domain/repository/EvaluacionCrediticiaRepository.java
package com.tufondo.creditos.domain.repository;

import com.tufondo.creditos.domain.model.EvaluacionCrediticia;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface para EvaluacionCrediticia.
 */
public interface EvaluacionCrediticiaRepository {
    
    /**
     * Guarda una evaluación crediticia.
     */
    EvaluacionCrediticia guardar(EvaluacionCrediticia evaluacion);
    
    /**
     * Busca evaluación por ID.
     */
    Optional<EvaluacionCrediticia> buscarPorId(UUID id);
    
    /**
     * Busca evaluación por ID de solicitud.
     */
    Optional<EvaluacionCrediticia> buscarPorSolicitudId(UUID solicitudId);
    
    /**
     * Verifica si existe evaluación para la solicitud.
     */
    boolean existePorSolicitudId(UUID solicitudId);
}
