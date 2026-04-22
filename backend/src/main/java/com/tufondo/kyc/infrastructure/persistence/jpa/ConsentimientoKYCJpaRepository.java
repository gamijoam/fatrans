// com.tufondo.kyc.infrastructure.persistence.jpa.ConsentimientoKYCJpaRepository
package com.tufondo.kyc.infrastructure.persistence.jpa;

import com.tufondo.kyc.infrastructure.persistence.entity.ConsentimientoKYCEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConsentimientoKYCJpaRepository extends JpaRepository<ConsentimientoKYCEntity, UUID> {

    List<ConsentimientoKYCEntity> findBySocioIdOrderByFechaConsentimientoDesc(UUID socioId);

    Optional<ConsentimientoKYCEntity> findFirstBySocioIdOrderByFechaConsentimientoDesc(UUID socioId);

    Optional<ConsentimientoKYCEntity> findFirstBySocioIdAndAceptadoTrueOrderByFechaConsentimientoDesc(UUID socioId);

    boolean existsBySocioIdAndAceptadoTrue(UUID socioId);
}