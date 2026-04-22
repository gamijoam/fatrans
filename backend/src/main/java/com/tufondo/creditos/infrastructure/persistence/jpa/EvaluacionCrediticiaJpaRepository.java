// com/tufondo/creditos/infrastructure/persistence/jpa/EvaluacionCrediticiaJpaRepository.java
package com.tufondo.creditos.infrastructure.persistence.jpa;

import com.tufondo.creditos.infrastructure.persistence.entity.EvaluacionCrediticiaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository para EvaluacionCrediticiaEntity.
 */
@Repository
public interface EvaluacionCrediticiaJpaRepository extends JpaRepository<EvaluacionCrediticiaEntity, UUID> {
    
    Optional<EvaluacionCrediticiaEntity> findBySolicitudId(UUID solicitudId);
    
    boolean existsBySolicitudId(UUID solicitudId);
}
