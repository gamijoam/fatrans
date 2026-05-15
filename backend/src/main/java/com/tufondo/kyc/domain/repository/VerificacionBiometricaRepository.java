package com.tufondo.kyc.domain.repository;

import com.tufondo.kyc.domain.model.VerificacionBiometrica;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VerificacionBiometricaRepository {

    VerificacionBiometrica save(VerificacionBiometrica verificacion);

    Optional<VerificacionBiometrica> findById(UUID id);

    /** Búsqueda por session id devuelto por el proveedor (Didit). Útil al recibir el webhook. */
    Optional<VerificacionBiometrica> findByProveedorSessionId(String proveedor, String sessionId);

    List<VerificacionBiometrica> findByVerificacionKycId(UUID verificacionKycId);

    List<VerificacionBiometrica> findBySocioId(UUID socioId);

    /**
     * Borra todos los intentos biométricos asociados a un socio. Lo usa el flujo de
     * revocación de consentimiento (LOPDP Art. 7 — derecho al olvido).
     */
    void deleteAllBySocioId(UUID socioId);
}
