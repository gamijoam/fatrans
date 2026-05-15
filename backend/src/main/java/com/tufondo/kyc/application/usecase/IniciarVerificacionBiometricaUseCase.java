package com.tufondo.kyc.application.usecase;

import com.tufondo.kyc.domain.exception.KYCException;
import com.tufondo.kyc.domain.model.VerificacionBiometrica;
import com.tufondo.kyc.domain.model.VerificacionKYC;
import com.tufondo.kyc.domain.model.enums.EstadoBiometria;
import com.tufondo.kyc.domain.model.enums.EstadoIntentoBiometrico;
import com.tufondo.kyc.domain.model.port.BiometricVerificatorPort;
import com.tufondo.kyc.domain.repository.ConsentimientoBiometricoRepository;
import com.tufondo.kyc.domain.repository.VerificacionBiometricaRepository;
import com.tufondo.kyc.domain.repository.VerificacionKYCRepository;
import com.tufondo.socios.domain.model.Socio;
import com.tufondo.socios.domain.repository.SocioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Inicia una sesión biométrica con el proveedor configurado y persiste un intento
 * en estado PENDIENTE. Devuelve los datos para que el frontend monte el widget.
 *
 * Precondiciones:
 *  - El socio debe tener un consentimiento biométrico vigente (LOPDP).
 *  - Debe existir una VerificacionKYC activa para el socio (la auto-creada al aprobar
 *    la solicitud de registro).
 *
 * Si ya hay un intento en EN_PROGRESO se reutiliza (idempotencia básica).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IniciarVerificacionBiometricaUseCase {

    private final BiometricVerificatorPort biometricPort;
    private final VerificacionKYCRepository verificacionKYCRepository;
    private final VerificacionBiometricaRepository biometricaRepository;
    private final ConsentimientoBiometricoRepository consentimientoRepository;
    private final SocioRepository socioRepository;

    @Transactional
    public Resultado ejecutar(UUID socioId, String ipCliente, String userAgent) {

        // 1. Verificar consentimiento LOPDP vigente.
        consentimientoRepository.findVigenteBySocioId(socioId)
                .orElseThrow(() -> new KYCException(
                        "El socio no tiene consentimiento biométrico vigente. " +
                        "Debe aceptar la política LOPDP biométrica antes de continuar."));

        // 2. Cargar el socio (necesitamos email + nombre para Didit).
        Socio socio = socioRepository.buscarPorId(socioId)
                .orElseThrow(() -> new KYCException("Socio no encontrado"));

        // 3. Buscar la VerificacionKYC activa del socio.
        VerificacionKYC kyc = verificacionKYCRepository.findActiveBySocioId(socioId)
                .orElseThrow(() -> new KYCException(
                        "El socio no tiene una verificación KYC activa"));

        // 4. Crear sesión con el proveedor.
        String nombreCompleto = (socio.getPrimerNombre() + " " + socio.getPrimerApellido()).trim();
        BiometricVerificatorPort.BiometricSessionResponse session = biometricPort.iniciarSesion(
                new BiometricVerificatorPort.BiometricSessionRequest(
                        socioId, socio.getCorreoElectronico(), nombreCompleto, null, null
                )
        );

        // 5. Persistir intento PENDIENTE.
        VerificacionBiometrica intento = VerificacionBiometrica.builder()
                .verificacionKycId(kyc.getId())
                .socioId(socioId)
                .proveedor(biometricPort.getProveedor())
                .proveedorSessionId(session.sessionId())
                .proveedorWorkflowId(session.workflowId())
                .estado(EstadoIntentoBiometrico.PENDIENTE)
                .fechaInicio(LocalDateTime.now())
                // Los artefactos se borran a los 90 días por política LOPDP.
                .fechaExpiracionArtefactos(LocalDateTime.now().plusDays(90))
                .ipCliente(ipCliente)
                .userAgent(userAgent)
                .build();
        VerificacionBiometrica guardado = biometricaRepository.save(intento);

        // 6. Marcar la KYC como EN_PROGRESO en el cache.
        kyc.setEstadoBiometria(EstadoBiometria.EN_PROGRESO);
        verificacionKYCRepository.save(kyc);

        log.info("Sesión biométrica iniciada. socioId={} sessionId={} proveedor={}",
                socioId, session.sessionId(), biometricPort.getProveedor());

        return new Resultado(guardado.getId(), session.sessionId(), session.widgetUrl(), session.widgetToken());
    }

    public record Resultado(UUID intentoId, String sessionId, String widgetUrl, String widgetToken) {}
}
