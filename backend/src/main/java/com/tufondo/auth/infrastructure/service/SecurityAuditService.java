package com.tufondo.auth.infrastructure.service;

import com.tufondo.auth.domain.model.audit.SecurityEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

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
}
