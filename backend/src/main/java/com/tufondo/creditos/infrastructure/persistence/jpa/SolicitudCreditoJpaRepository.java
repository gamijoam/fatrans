// com/tufondo/creditos/infrastructure/persistence/jpa/SolicitudCreditoJpaRepository.java
package com.tufondo.creditos.infrastructure.persistence.jpa;

import com.tufondo.creditos.domain.model.enums.EstadoSolicitud;
import com.tufondo.creditos.infrastructure.persistence.entity.SolicitudCreditoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository para SolicitudCreditoEntity.
 */
@Repository
public interface SolicitudCreditoJpaRepository extends JpaRepository<SolicitudCreditoEntity, UUID>, JpaSpecificationExecutor<SolicitudCreditoEntity> {
    
    Optional<SolicitudCreditoEntity> findByNumeroSolicitud(String numeroSolicitud);
    
    List<SolicitudCreditoEntity> findBySocioId(UUID socioId);

    List<SolicitudCreditoEntity> findByEstado(EstadoSolicitud estado);

    boolean existsBySocioIdAndEstado(UUID socioId, EstadoSolicitud estado);
    
    boolean existsByNumeroSolicitud(String numeroSolicitud);

    long countByEstado(EstadoSolicitud estado);

    @Query("SELECT COUNT(s) FROM SolicitudCreditoEntity s WHERE s.estado = :estado AND s.createdAt >= :fecha")
    long countByEstadoAndCreatedAtAfter(@Param("estado") EstadoSolicitud estado, @Param("fecha") LocalDateTime fecha);

    @Query("SELECT COALESCE(SUM(s.montoSolicitado), 0) FROM SolicitudCreditoEntity s WHERE s.estado = :estado")
    BigDecimal sumMontoSolicitadoByEstado(@Param("estado") EstadoSolicitud estado);
}
