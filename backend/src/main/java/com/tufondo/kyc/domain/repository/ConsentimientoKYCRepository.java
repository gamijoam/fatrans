// com.tufondo.kyc.domain.repository.ConsentimientoKYCRepository
package com.tufondo.kyc.domain.repository;

import com.tufondo.kyc.domain.model.ConsentimientoKYC;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface para ConsentimientoKYC.
 */
public interface ConsentimientoKYCRepository {

    Optional<ConsentimientoKYC> findById(UUID id);

    List<ConsentimientoKYC> findBySocioIdOrderByFechaConsentimientoDesc(UUID socioId);

    Optional<ConsentimientoKYC> findLatestBySocioId(UUID socioId);

    Optional<ConsentimientoKYC> findActiveBySocioId(UUID socioId);

    boolean existsBySocioIdAndAceptadoTrue(UUID socioId);

    ConsentimientoKYC save(ConsentimientoKYC consentimiento);
}