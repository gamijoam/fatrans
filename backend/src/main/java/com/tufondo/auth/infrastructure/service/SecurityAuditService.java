package com.tufondo.auth.infrastructure.service;

import com.tufondo.auth.domain.model.audit.SecurityEvent;
import com.tufondo.auth.infrastructure.persistence.entity.SecurityAuditEntity;
import com.tufondo.auth.infrastructure.persistence.jpa.SecurityAuditJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityAuditService {

    private final SecurityAuditJpaRepository auditRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logLoginExitoso(String usuarioId, String ip) {
        SecurityEvent event = SecurityEvent.loginExitoso(
                UUID.fromString(usuarioId),
                ip
        );
        persistEvent(event);
        log.info("AUDIT [{}] usuario={} ip={} tipo={}",
                event.timestamp(),
                event.usuarioId(),
                event.ipAddress(),
                event.tipoEvento()
        );
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logLoginFallido(String identificador, String ip, String razon) {
        SecurityEvent event = SecurityEvent.loginFallido(identificador, ip, razon);
        persistEvent(event);
        log.warn("AUDIT [{}] ip={} tipo={} detalles={}",
                event.timestamp(),
                event.ipAddress(),
                event.tipoEvento(),
                event.detalles()
        );
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logLogout(String usuarioId, String ip) {
        SecurityEvent event = SecurityEvent.logout(
                UUID.fromString(usuarioId),
                ip
        );
        persistEvent(event);
        log.info("AUDIT [{}] usuario={} ip={} tipo={}",
                event.timestamp(),
                event.usuarioId(),
                event.ipAddress(),
                event.tipoEvento()
        );
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logTokenRefresh(String usuarioId, String ip) {
        SecurityEvent event = SecurityEvent.tokenRefresh(
                UUID.fromString(usuarioId),
                ip
        );
        persistEvent(event);
        log.info("AUDIT [{}] usuario={} ip={} tipo={}",
                event.timestamp(),
                event.usuarioId(),
                event.ipAddress(),
                event.tipoEvento()
        );
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logCuentaBloqueada(String usuarioId, String ip) {
        SecurityEvent event = SecurityEvent.cuentaBloqueada(
                UUID.fromString(usuarioId),
                ip
        );
        persistEvent(event);
        log.warn("AUDIT [{}] usuario={} ip={} tipo={} detalles={}",
                event.timestamp(),
                event.usuarioId(),
                event.ipAddress(),
                event.tipoEvento(),
                event.detalles()
        );
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logDashboardAcceso(String usuarioId, String ip, String rol) {
        SecurityEvent event = SecurityEvent.dashboardAcceso(
                UUID.fromString(usuarioId),
                ip,
                rol
        );
        persistEvent(event);
        log.info("AUDIT [{}] usuario={} ip={} tipo={} detalles={}",
                event.timestamp(),
                event.usuarioId(),
                event.ipAddress(),
                event.tipoEvento(),
                event.detalles()
        );
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarIntentoVerificacion(UUID usuarioId, String tipoEvento, boolean exitoso, String ip) {
        SecurityEvent event = SecurityEvent.builder()
                .id(UUID.randomUUID())
                .usuarioId(usuarioId)
                .tipoEvento(tipoEvento)
                .timestamp(Instant.now())
                .ipAddress(ip)
                .detalles(switch (tipoEvento) {
                    case "PASSWORD_VERIFIED" -> "Verificación de password exitosa";
                    case "CODIGO_ENVIADO_EMAIL" -> "Código de verificación enviado por email";
                    case "CODIGO_ENVIADO_SMS" -> "Código de verificación enviado por SMS";
                    case "CODIGO_CONFIRMED" -> "Código de verificación confirmado";
                    default -> "Intento de verificación: " + tipoEvento;
                })
                .build();
        persistEvent(event);
        if (exitoso) {
            log.info("AUDIT [{}] usuario={} ip={} tipo={} detalles={}",
                    event.timestamp(),
                    event.usuarioId(),
                    event.ipAddress(),
                    event.tipoEvento(),
                    event.detalles()
            );
        } else {
            log.warn("AUDIT [{}] usuario={} ip={} tipo={} detalles={}",
                    event.timestamp(),
                    event.usuarioId(),
                    event.ipAddress(),
                    event.tipoEvento(),
                    event.detalles()
            );
        }
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSesionesInvalidadas(UUID usuarioId, String ip, UUID invalidadoPor, int cantidad) {
        SecurityEvent event = SecurityEvent.sesionesInvalidadas(usuarioId, ip, invalidadoPor, cantidad);
        persistEvent(event);
        log.warn("AUDIT [{}] usuario={} ip={} tipo={} detalles={}",
                event.timestamp(),
                event.usuarioId(),
                event.ipAddress(),
                event.tipoEvento(),
                event.detalles()
        );
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSesionIndividualInvalidadas(UUID usuarioId, String ip, UUID invalidadoPor, String sesionId) {
        SecurityEvent event = SecurityEvent.sesionIndividualInvalidadas(usuarioId, ip, invalidadoPor, sesionId);
        persistEvent(event);
        log.warn("AUDIT [{}] usuario={} ip={} tipo={} detalles={}",
                event.timestamp(),
                event.usuarioId(),
                event.ipAddress(),
                event.tipoEvento(),
                event.detalles()
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logCustomEvent(String tipoEvento, UUID usuarioId, String ip, String detalles) {
        SecurityEvent event = SecurityEvent.builder()
                .id(UUID.randomUUID())
                .tipoEvento(tipoEvento)
                .usuarioId(usuarioId)
                .timestamp(Instant.now())
                .ipAddress(ip)
                .detalles(detalles)
                .build();
        persistEvent(event);
        log.info("AUDIT [{}] usuario={} ip={} tipo={} detalles={}",
                event.timestamp(),
                event.usuarioId(),
                event.ipAddress(),
                event.tipoEvento(),
                event.detalles()
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logEntityEvent(String tipoEvento, UUID usuarioId, String ip, String entityType, String entityId, String action, String detalles) {
        SecurityAuditEntity entity = SecurityAuditEntity.builder()
                .id(UUID.randomUUID())
                .tipoEvento(tipoEvento)
                .usuarioId(usuarioId)
                .ipAddress(ip)
                .timestamp(Instant.now())
                .detalles(detalles)
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .build();
        auditRepository.save(entity);
        log.info("AUDIT [{}] usuario={} ip={} tipo={} entity={}/{} action={} detalles={}",
                entity.getTimestamp(),
                usuarioId,
                ip,
                tipoEvento,
                entityType,
                entityId,
                action,
                detalles
        );
    }

    private void persistEvent(SecurityEvent event) {
        SecurityAuditEntity entity = SecurityAuditEntity.builder()
                .id(event.id())
                .tipoEvento(event.tipoEvento())
                .usuarioId(event.usuarioId())
                .ipAddress(event.ipAddress())
                .timestamp(event.timestamp())
                .detalles(event.detalles())
                .build();
        try {
            auditRepository.save(entity);
        } catch (Exception e) {
            log.error("Error al persistir evento de auditoría: {}", e.getMessage());
        }
    }
}