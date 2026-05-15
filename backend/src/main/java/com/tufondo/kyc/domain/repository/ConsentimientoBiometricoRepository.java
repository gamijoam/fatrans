package com.tufondo.kyc.domain.repository;

import com.tufondo.kyc.domain.model.ConsentimientoBiometrico;

import java.util.Optional;
import java.util.UUID;

public interface ConsentimientoBiometricoRepository {

    ConsentimientoBiometrico save(ConsentimientoBiometrico consentimiento);

    /** Devuelve el consentimiento vigente (aceptado + no revocado) más reciente del socio. */
    Optional<ConsentimientoBiometrico> findVigenteBySocioId(UUID socioId);

    Optional<ConsentimientoBiometrico> findById(UUID id);
}
