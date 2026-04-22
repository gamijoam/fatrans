// com/tufondo/creditos/infrastructure/persistence/jpa/PlanAmortizacionJpaRepository.java
package com.tufondo.creditos.infrastructure.persistence.jpa;

import com.tufondo.creditos.infrastructure.persistence.entity.PlanAmortizacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository para PlanAmortizacionEntity.
 */
@Repository
public interface PlanAmortizacionJpaRepository extends JpaRepository<PlanAmortizacionEntity, UUID> {
    
    Optional<PlanAmortizacionEntity> findBySolicitudId(UUID solicitudId);
    
    boolean existsBySolicitudId(UUID solicitudId);
}
