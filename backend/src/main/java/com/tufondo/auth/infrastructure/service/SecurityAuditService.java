package com.tufondo.auth.infrastructure.service;

import com.tufondo.auth.domain.model.audit.SecurityEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
public class SecurityAuditService {

    public void logLoginExitoso(String usuarioId, String ip) {
        SecurityEvent event = SecurityEvent.loginExitoso(
                java.util.UUID.fromString(usuarioId),
                ip
        );
        log.info("AUDIT [{}] usuario={} ip={} tipo={}",
                event.timestamp(),
                event.usuarioId(),
                event.ipAddress(),
                event.tipoEvento()
        );
    }

    public void logLoginFallido(String identificador, String ip, String razon) {
        SecurityEvent event = SecurityEvent.loginFallido(identificador, ip, razon);
        log.warn("AUDIT [{}] ip={} tipo={} detalles={}",
                event.timestamp(),
                event.ipAddress(),
                event.tipoEvento(),
                event.detalles()
        );
    }

    public void logLogout(String usuarioId, String ip) {
        SecurityEvent event = SecurityEvent.logout(
                java.util.UUID.fromString(usuarioId),
                ip
        );
        log.info("AUDIT [{}] usuario={} ip={} tipo={}",
                event.timestamp(),
                event.usuarioId(),
                event.ipAddress(),
                event.tipoEvento()
        );
    }

    public void logTokenRefresh(String usuarioId, String ip) {
        SecurityEvent event = SecurityEvent.tokenRefresh(
                java.util.UUID.fromString(usuarioId),
                ip
        );
        log.info("AUDIT [{}] usuario={} ip={} tipo={}",
                event.timestamp(),
                event.usuarioId(),
                event.ipAddress(),
                event.tipoEvento()
        );
    }

    public void logCuentaBloqueada(String usuarioId, String ip) {
        SecurityEvent event = SecurityEvent.cuentaBloqueada(
                java.util.UUID.fromString(usuarioId),
                ip
        );
        log.warn("AUDIT [{}] usuario={} ip={} tipo={} detalles={}",
                event.timestamp(),
                event.usuarioId(),
                event.ipAddress(),
                event.tipoEvento(),
                event.detalles()
        );
    }

    public void logDashboardAcceso(String usuarioId, String ip, String rol) {
        SecurityEvent event = SecurityEvent.dashboardAcceso(
                java.util.UUID.fromString(usuarioId),
                ip,
                rol
        );
        log.info("AUDIT [{}] usuario={} ip={} tipo={} detalles={}",
                event.timestamp(),
                event.usuarioId(),
                event.ipAddress(),
                event.tipoEvento(),
                event.detalles()
        );
    }

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

    public void logSesionesInvalidadas(UUID usuarioId, String ip, UUID invalidadoPor, int cantidad) {
        SecurityEvent event = SecurityEvent.sesionesInvalidadas(usuarioId, ip, invalidadoPor, cantidad);
        log.warn("AUDIT [{}] usuario={} ip={} tipo={} detalles={}",
                event.timestamp(),
                event.usuarioId(),
                event.ipAddress(),
                event.tipoEvento(),
                event.detalles()
        );
    }

    public void logSesionIndividualInvalidadas(UUID usuarioId, String ip, UUID invalidadoPor, String sesionId) {
        SecurityEvent event = SecurityEvent.sesionIndividualInvalidadas(usuarioId, ip, invalidadoPor, sesionId);
        log.warn("AUDIT [{}] usuario={} ip={} tipo={} detalles={}",
                event.timestamp(),
                event.usuarioId(),
                event.ipAddress(),
                event.tipoEvento(),
                event.detalles()
        );
    }
}
