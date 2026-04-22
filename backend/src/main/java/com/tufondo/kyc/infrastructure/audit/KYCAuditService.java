// com.tufondo.kyc.infrastructure.audit.KYCAuditService
package com.tufondo.kyc.infrastructure.audit;

import com.tufondo.kyc.infrastructure.persistence.entity.AuditKYCEntity;
import com.tufondo.kyc.infrastructure.persistence.entity.AuditKYCEntity.TipoEventoAuditoria;
import com.tufondo.kyc.infrastructure.persistence.jpa.AuditKYCJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Servicio de auditoría KYC para cumplimiento regulatorio (LOPDP/SUDEBAN).
 * Registra todos los accesos y operaciones realizadas sobre datos KYC.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KYCAuditService {

    private final AuditKYCJpaRepository auditRepository;

    /**
     * Registra un evento de auditoría de forma asíncrona.
     */
    @Async
    public void registrarEvento(TipoEventoAuditoria tipoEvento, UUID socioId, String usuarioId,
                                String rolUsuario, String endpoint, String metodoHttp,
                                String ipCliente, String userAgent, String descripcion,
                                UUID verificacionId, UUID documentoId,
                                String estadoAnterior, String estadoNuevo,
                                Boolean exitoso, String codigoError) {
        try {
            AuditKYCEntity audit = AuditKYCEntity.builder()
                .tipoEvento(tipoEvento)
                .socioId(socioId)
                .usuarioId(usuarioId)
                .rolUsuario(rolUsuario)
                .endpoint(endpoint)
                .metodoHttp(metodoHttp)
                .ipCliente(ipCliente)
                .userAgent(userAgent)
                .descripcion(descripcion)
                .verificacionId(verificacionId)
                .documentoId(documentoId)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(estadoNuevo)
                .exitoso(exitoso)
                .codigoError(codigoError)
                .fechaEvento(LocalDateTime.now())
                .build();

            auditRepository.save(audit);
            log.debug("Auditoría KYC registrada: {} - {} - {}", tipoEvento, usuarioId, socioId);
        } catch (Exception e) {
            log.error("Error al registrar auditoría KYC: {}", e.getMessage(), e);
        }
    }

    /**
     * Registra acceso a verificación.
     */
    public void registrarAccesoVerificacion(UUID socioId, String usuarioId, String rolUsuario,
                                            String endpoint, String ipCliente, String userAgent,
                                            UUID verificacionId, boolean exitoso) {
        registrarEvento(
            TipoEventoAuditoria.ACCESO_VERIFICACION, socioId, usuarioId, rolUsuario,
            endpoint, "GET", ipCliente, userAgent,
            "Acceso a detalle de verificacion KYC",
            verificacionId, null, null, null, exitoso, null
        );
    }

    /**
     * Registra aprobación de verificación.
     */
    public void registrarAprobacion(UUID socioId, String usuarioId, String rolUsuario,
                                   String endpoint, String ipCliente, String userAgent,
                                   UUID verificacionId, String estadoAnterior) {
        registrarEvento(
            TipoEventoAuditoria.VERIFICACION_ACEPTADA, socioId, usuarioId, rolUsuario,
            endpoint, "POST", ipCliente, userAgent,
            "Verificacion KYC aprobada",
            verificacionId, null, estadoAnterior, "APROBADO", true, null
        );
    }

    /**
     * Registra rechazo de verificación.
     */
    public void registrarRechazo(UUID socioId, String usuarioId, String rolUsuario,
                                 String endpoint, String ipCliente, String userAgent,
                                 UUID verificacionId, String estadoAnterior, String motivo) {
        registrarEvento(
            TipoEventoAuditoria.VERIFICACION_RECHAZADA, socioId, usuarioId, rolUsuario,
            endpoint, "POST", ipCliente, userAgent,
            "Verificacion KYC rechazada: " + motivo,
            verificacionId, null, estadoAnterior, "RECHAZADO", true, null
        );
    }

    /**
     * Registra intento de acceso denegado (IDOR).
     */
    public void registrarAccesoDenegadoIdor(UUID socioId, String usuarioId, String rolUsuario,
                                           String endpoint, String ipCliente, String userAgent,
                                           UUID verificacionId, String motivo) {
        registrarEvento(
            TipoEventoAuditoria.ACCESO_DENEGADO_IDOR, socioId, usuarioId, rolUsuario,
            endpoint, "GET", ipCliente, userAgent,
            "Intento de acceso IDOR detectado: " + motivo,
            verificacionId, null, null, null, false, "KYC_009"
        );
    }

    /**
     * Registra rate limit excedido.
     */
    public void registrarRateLimitExcedido(String usuarioId, String ipCliente,
                                            String endpoint, String metodoHttp) {
        registrarEvento(
            TipoEventoAuditoria.RATE_LIMIT_EXCEDIDO, null, usuarioId, null,
            endpoint, metodoHttp, ipCliente, null,
            "Rate limit excedido",
            null, null, null, null, false, "RATE_LIMIT"
        );
    }
}