package com.tufondo.kyc.application.usecase;

import com.tufondo.kyc.domain.model.VerificacionBiometrica;
import com.tufondo.kyc.domain.model.VerificacionKYC;
import com.tufondo.kyc.domain.model.enums.EstadoBiometria;
import com.tufondo.kyc.domain.model.port.BiometricVerificatorPort;
import com.tufondo.kyc.domain.repository.VerificacionBiometricaRepository;
import com.tufondo.kyc.domain.repository.VerificacionKYCRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Procesa un webhook del proveedor biométrico (Didit, etc.):
 *  - El adapter ya verificó la firma HMAC + ventana de timestamp.
 *  - Aquí solo aplicamos el resultado normalizado: actualizamos el intento y el cache
 *    de estado en la VerificacionKYC.
 *
 * Idempotente: si el webhook se reenvía (Didit reintenta hasta confirmar 2xx), el
 * segundo procesamiento detecta el estado final y no hace cambios destructivos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProcesarWebhookBiometricoUseCase {

    private final BiometricVerificatorPort biometricPort;
    private final VerificacionBiometricaRepository biometricaRepository;
    private final VerificacionKYCRepository kycRepository;

    @Transactional
    public void ejecutar(byte[] rawBody, String signatureHeader, String timestampHeader) {
        BiometricVerificatorPort.BiometricWebhookResult result =
                biometricPort.procesarWebhook(rawBody, signatureHeader, timestampHeader);

        // Si la sesión no existe en nuestra BD (e.g. fue creada fuera del flow normal,
        // o un Test Webhook de Didit con session_id sintético), no es un error:
        // devolvemos sin hacer nada para que Didit no reintente indefinidamente.
        // Loggeamos para auditoría, pero el HTTP queda 200 (no 500).
        java.util.Optional<VerificacionBiometrica> intentoOpt = biometricaRepository
                .findByProveedorSessionId(biometricPort.getProveedor(), result.sessionId());
        if (intentoOpt.isEmpty()) {
            log.warn("Webhook ignorado: no existe intento biométrico para sessionId={} (provider={}). " +
                    "Esto es normal para Test Webhooks o sesiones creadas fuera del flujo.",
                    result.sessionId(), biometricPort.getProveedor());
            return;
        }
        VerificacionBiometrica intento = intentoOpt.get();

        // Idempotencia: si ya está en estado final, no reaplicar.
        if (intento.getEstado() == com.tufondo.kyc.domain.model.enums.EstadoIntentoBiometrico.APROBADA
                || intento.getEstado() == com.tufondo.kyc.domain.model.enums.EstadoIntentoBiometrico.RECHAZADA) {
            log.info("Webhook duplicado para sessionId={} (estado={}); ignorado.",
                    result.sessionId(), intento.getEstado());
            return;
        }

        VerificacionBiometrica actualizado;
        EstadoBiometria nuevoEstadoCache;
        switch (result.outcome()) {
            case APROBADO -> {
                actualizado = intento.aprobar(
                        result.livenessScore(), result.faceMatchScore(), result.documentOcrScore());
                nuevoEstadoCache = EstadoBiometria.APROBADA;
            }
            case RECHAZADO -> {
                actualizado = intento.rechazar(result.motivoFallo(), result.tipoAtaqueDetectado());
                nuevoEstadoCache = EstadoBiometria.RECHAZADA;
            }
            case EXPIRADO -> {
                actualizado = intento.expirar();
                nuevoEstadoCache = EstadoBiometria.EXPIRADA;
            }
            case CANCELADO -> {
                actualizado = intento.cancelar();
                // Permitir reintento: el cache vuelve a NO_INICIADA.
                nuevoEstadoCache = EstadoBiometria.NO_INICIADA;
            }
            default -> {
                // EN_PROGRESO: solo actualizamos timestamp, no estado final.
                log.debug("Webhook intermedio para sessionId={} (en progreso)", result.sessionId());
                return;
            }
        }

        biometricaRepository.save(actualizado);

        // Actualizar cache en VerificacionKYC.
        VerificacionKYC kyc = kycRepository.findById(intento.getVerificacionKycId()).orElse(null);
        if (kyc != null) {
            kyc.setEstadoBiometria(nuevoEstadoCache);
            kycRepository.save(kyc);
        }

        log.info("Webhook biométrico aplicado. sessionId={} outcome={} liveness={} match={}",
                result.sessionId(), result.outcome(), result.livenessScore(), result.faceMatchScore());
    }
}
