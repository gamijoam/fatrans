package com.tufondo.kyc.application.usecase;

import com.tufondo.kyc.domain.model.VerificacionBiometrica;
import com.tufondo.kyc.domain.model.VerificacionKYC;
import com.tufondo.kyc.domain.model.enums.EstadoBiometria;
import com.tufondo.kyc.domain.model.enums.EstadoIntentoBiometrico;
import com.tufondo.kyc.domain.model.port.BiometricVerificatorPort;
import com.tufondo.kyc.domain.repository.VerificacionBiometricaRepository;
import com.tufondo.kyc.domain.repository.VerificacionKYCRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Sincroniza el estado biométrico de un socio consultando directamente al
 * proveedor (Didit) en lugar de esperar al webhook. Necesario cuando:
 *  - El webhook no está configurado en el dashboard del proveedor.
 *  - El webhook llegó pero falló (firma inválida, network blip, etc.).
 *  - Queremos UX más responsiva — el frontend hace polling cada 5s y
 *    fuerza sync sin esperar al webhook asíncrono.
 *
 * Es idempotente: si el intento ya está en estado final (APROBADA/RECHAZADA)
 * no llama al proveedor y devuelve el estado actual. Si la sesión sigue en
 * pending del lado del proveedor, no muta nada.
 *
 * Diseñado para ser invocado **muchas veces** (polling): la única llamada
 * externa es la consulta a Didit, idempotente por su API, no genera efectos
 * secundarios cuando no hay novedad.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SincronizarVerificacionBiometricaUseCase {

    private final BiometricVerificatorPort biometricPort;
    private final VerificacionBiometricaRepository biometricaRepository;
    private final VerificacionKYCRepository kycRepository;

    /**
     * @param socioId el socio cuyo último intento biométrico queremos sincronizar
     * @return el estado actual de la biometría del socio (puede no haber cambiado)
     */
    @Transactional
    public EstadoBiometria sincronizar(UUID socioId) {
        // 1. Buscar el último intento biométrico del socio. Si no hay, no
        //    tiene sentido sincronizar — devolvemos NO_INICIADA.
        Optional<VerificacionBiometrica> ultimoIntento =
                biometricaRepository.findLastBySocioId(socioId);
        if (ultimoIntento.isEmpty()) {
            return EstadoBiometria.NO_INICIADA;
        }
        VerificacionBiometrica intento = ultimoIntento.get();

        // 2. Idempotencia local: si ya está en estado final, no llamamos al
        //    proveedor — el resultado no puede cambiar.
        EstadoIntentoBiometrico estadoActual = intento.getEstado();
        if (estadoActual == EstadoIntentoBiometrico.APROBADA) {
            return EstadoBiometria.APROBADA;
        }
        if (estadoActual == EstadoIntentoBiometrico.RECHAZADA) {
            return EstadoBiometria.RECHAZADA;
        }
        if (estadoActual == EstadoIntentoBiometrico.EXPIRADA) {
            return EstadoBiometria.EXPIRADA;
        }

        // 3. Estado intermedio (PENDIENTE/EN_PROGRESO): pull al proveedor.
        BiometricVerificatorPort.BiometricWebhookResult result;
        try {
            result = biometricPort.consultarDecision(intento.getProveedorSessionId());
        } catch (Exception e) {
            log.warn("Sync biométrico falló al consultar proveedor (socioId={} session={}): {}",
                    socioId, intento.getProveedorSessionId(), e.getMessage());
            // Si el proveedor falla, devolvemos el estado actual del cache —
            // el siguiente polling reintentará. No queremos romper el frontend.
            return EstadoBiometria.EN_PROGRESO;
        }

        // 4. Aplicar transición — misma lógica que ProcesarWebhookBiometricoUseCase.
        VerificacionBiometrica actualizado;
        EstadoBiometria nuevoCache;
        switch (result.outcome()) {
            case APROBADO -> {
                actualizado = intento.aprobar(
                        result.livenessScore(), result.faceMatchScore(), result.documentOcrScore());
                nuevoCache = EstadoBiometria.APROBADA;
            }
            case RECHAZADO -> {
                actualizado = intento.rechazar(result.motivoFallo(), result.tipoAtaqueDetectado());
                nuevoCache = EstadoBiometria.RECHAZADA;
            }
            case EXPIRADO -> {
                actualizado = intento.expirar();
                nuevoCache = EstadoBiometria.EXPIRADA;
            }
            case CANCELADO -> {
                actualizado = intento.cancelar();
                nuevoCache = EstadoBiometria.NO_INICIADA;
            }
            default -> {
                // EN_PROGRESO en el proveedor — todavía no terminó. No mutamos.
                return EstadoBiometria.EN_PROGRESO;
            }
        }
        biometricaRepository.save(actualizado);

        // 5. Actualizar cache en VerificacionKYC.
        VerificacionKYC kyc = kycRepository.findById(intento.getVerificacionKycId()).orElse(null);
        if (kyc != null && kyc.getEstadoBiometria() != nuevoCache) {
            kyc.setEstadoBiometria(nuevoCache);
            kycRepository.save(kyc);
        }

        log.info("Sync biométrico aplicado. socioId={} session={} outcome={}",
                socioId, intento.getProveedorSessionId(), result.outcome());
        return nuevoCache;
    }
}
