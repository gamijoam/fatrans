// com/tufondo/creditos/domain/repository/AmortizacionRepository.java
package com.tufondo.creditos.domain.repository;

import com.tufondo.creditos.domain.model.Amortizacion;
import com.tufondo.creditos.domain.model.enums.EstadoAmortizacion;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface para Amortizacion.
 */
public interface AmortizacionRepository {
    
    /**
     * Guarda una amortización.
     */
    Amortizacion guardar(Amortizacion amortizacion);
    
    /**
     * Guarda varias amortizaciones en batch.
     */
    List<Amortizacion> guardarBatch(List<Amortizacion> amortizaciones);
    
    /**
     * Busca amortización por ID.
     */
    Optional<Amortizacion> buscarPorId(UUID id);
    
    /**
     * Busca amortización por ID con lock (para pagos concurrentes).
     */
    Optional<Amortizacion> buscarPorIdWithLock(UUID id);
    
    /**
     * Lista amortizaciones por ID de plan.
     */
    List<Amortizacion> listarPorPlanId(UUID planId);
    
    /**
     * Lista amortizaciones por estado.
     */
    List<Amortizacion> listarPorEstado(EstadoAmortizacion estado);
    
    /**
     * Busca amortización por referencia de pago (idempotencia).
     */
    Optional<Amortizacion> buscarPorReferenciaPago(String referenciaPago);
    
    /**
     * Verifica si existe amortización con la referencia de pago.
     */
    boolean existePorReferenciaPago(String referenciaPago);

    long countByEstado(EstadoAmortizacion estado);
    BigDecimal sumInteresesMoraPendientes();
}
