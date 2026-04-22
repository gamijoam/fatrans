// com/tufondo/creditos/infrastructure/persistence/jpa/AmortizacionJpaRepository.java
package com.tufondo.creditos.infrastructure.persistence.jpa;

import com.tufondo.creditos.domain.model.enums.EstadoAmortizacion;
import com.tufondo.creditos.infrastructure.persistence.entity.AmortizacionEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository para AmortizacionEntity.
 */
@Repository
public interface AmortizacionJpaRepository extends JpaRepository<AmortizacionEntity, UUID> {
    
    List<AmortizacionEntity> findByPlanId(UUID planId);
    
    List<AmortizacionEntity> findByEstado(EstadoAmortizacion estado);
    
    Optional<AmortizacionEntity> findByReferenciaPago(String referenciaPago);
    
    boolean existsByReferenciaPago(String referenciaPago);
    
    /**
     * Busca por ID con lock pesimista para prevenir double-payment.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AmortizacionEntity a WHERE a.id = :id")
    Optional<AmortizacionEntity> findByIdWithLock(@Param("id") UUID id);

    long countByEstado(EstadoAmortizacion estado);

    @Query("SELECT COALESCE(SUM(a.interesMora), 0) FROM AmortizacionEntity a WHERE a.interesMora IS NOT NULL AND a.estado NOT IN ('PAGADA', 'CANCELADA')")
    BigDecimal sumInteresesMoraPendientes();
}
