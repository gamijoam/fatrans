package com.tufondo.kyc.application.usecase;

import com.tufondo.kyc.domain.exception.KYCException;
import com.tufondo.kyc.domain.model.ConsentimientoBiometrico;
import com.tufondo.kyc.domain.model.VerificacionBiometrica;
import com.tufondo.kyc.domain.model.port.BiometricVerificatorPort;
import com.tufondo.kyc.domain.repository.ConsentimientoBiometricoRepository;
import com.tufondo.kyc.domain.repository.VerificacionBiometricaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Revoca el consentimiento biométrico del socio y solicita borrado al proveedor
 * (LOPDP Art. 7 — derecho a revocación + derecho al olvido).
 *
 * Flujo:
 *  1. Marca el consentimiento vigente con fecha_revocacion.
 *  2. Para cada intento biométrico del socio, solicita a Didit el borrado de la sesión.
 *  3. Borra los registros locales de intentos biométricos (datos biométricos no se
 *     deben retener tras revocación).
 *
 * Errores parciales (Didit no responde) se loggean pero NO bloquean la revocación
 * local — la prioridad es honrar el derecho del titular; el borrado en el proveedor
 * se reintenta async después.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RevocarConsentimientoBiometricoUseCase {

    private final ConsentimientoBiometricoRepository consentimientoRepository;
    private final VerificacionBiometricaRepository biometricaRepository;
    private final BiometricVerificatorPort biometricPort;

    @Transactional
    public void ejecutar(UUID socioId) {
        ConsentimientoBiometrico vigente = consentimientoRepository.findVigenteBySocioId(socioId)
                .orElseThrow(() -> new KYCException(
                        "No hay consentimiento biométrico vigente para revocar"));

        // 1. Solicitar borrado en el proveedor por cada intento conocido.
        // Para el volumen actual (1-10 intentos/socio) va inline. Si crece, mover a job async.
        List<VerificacionBiometrica> intentos = biometricaRepository.findBySocioId(socioId);

        for (VerificacionBiometrica intento : intentos) {
            try {
                biometricPort.solicitarBorradoSesion(intento.getProveedorSessionId());
            } catch (Exception e) {
                // No bloquea — el regulador prioriza la revocación local. Se reintenta async.
                log.error("Borrado en proveedor falló para sessionId={}: {}",
                        intento.getProveedorSessionId(), e.getMessage());
            }
        }

        // 2. Borrar registros locales.
        biometricaRepository.deleteAllBySocioId(socioId);

        // 3. Marcar consentimiento como revocado.
        ConsentimientoBiometrico revocado = vigente.revocar();
        consentimientoRepository.save(revocado);

        log.info("Consentimiento biométrico revocado para socioId={}", socioId);
    }
}
