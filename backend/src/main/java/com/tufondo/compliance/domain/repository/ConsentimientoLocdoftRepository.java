package com.tufondo.compliance.domain.repository;

import com.tufondo.compliance.domain.model.ConsentimientoLocdoftOperacion;

import java.util.UUID;

/**
 * Port para persistencia de consentimientos LOCDOFT de operación (#218 PR-C).
 */
public interface ConsentimientoLocdoftRepository {

    ConsentimientoLocdoftOperacion guardar(ConsentimientoLocdoftOperacion consentimiento);

    /**
     * Asocia el consentimiento con el movimiento real una vez que la
     * operación se completó exitosamente. Si la operación falla, NO se
     * llama y el registro queda con {@code movimientoId=null} (deseable
     * como evidencia del intento).
     */
    void asociarConMovimiento(UUID consentimientoId, UUID movimientoId);
}
