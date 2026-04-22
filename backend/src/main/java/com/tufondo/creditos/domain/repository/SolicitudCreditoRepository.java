// com/tufondo/creditos/domain/repository/SolicitudCreditoRepository.java
package com.tufondo.creditos.domain.repository;

import com.tufondo.creditos.domain.model.SolicitudCredito;
import com.tufondo.creditos.domain.model.enums.EstadoSolicitud;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface para SolicitudCredito.
 */
public interface SolicitudCreditoRepository {
    
    /**
     * Guarda una solicitud de crédito.
     */
    SolicitudCredito guardar(SolicitudCredito solicitud);
    
    /**
     * Busca solicitud por ID.
     */
    Optional<SolicitudCredito> buscarPorId(UUID id);
    
    /**
     * Busca solicitud por número de solicitud.
     */
    Optional<SolicitudCredito> buscarPorNumeroSolicitud(String numeroSolicitud);
    
    /**
     * Lista solicitudes por socio.
     */
    List<SolicitudCredito> listarPorSocioId(UUID socioId);

    /**
     * Lista solicitudes por estado.
     */
    List<SolicitudCredito> listarPorEstado(EstadoSolicitud estado);

    /**
     * Verifica si existe una solicitud activa (DESEMBOLSADO) para el socio.
     */
    boolean existeCreditoActivoPorSocio(UUID socioId);
    
    /**
     * Lista todas las solicitudes.
     */
    List<SolicitudCredito> listarTodos();

    /**
     * Verifica si existe el número de solicitud.
     */
    boolean existePorNumeroSolicitud(String numeroSolicitud);

    long countByEstado(EstadoSolicitud estado);
    long countByEstadoAndCreatedAtAfter(EstadoSolicitud estado, LocalDateTime fecha);
    BigDecimal sumMontoSolicitadoByEstado(EstadoSolicitud estado);
}
