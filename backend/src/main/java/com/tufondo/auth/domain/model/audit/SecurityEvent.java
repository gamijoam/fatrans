package com.tufondo.auth.domain.model.audit;

import java.time.Instant;
import java.util.UUID;

public record SecurityEvent(
        UUID id,
        String tipoEvento,
        UUID usuarioId,
        String ipAddress,
        Instant timestamp,
        String detalles
) {
    public static SecurityEvent loginExitoso(UUID usuarioId, String ip) {
        return new SecurityEvent(
                UUID.randomUUID(),
                "LOGIN_SUCCESS",
                usuarioId,
                ip,
                Instant.now(),
                null
        );
    }

    public static SecurityEvent loginFallido(String identificador, String ip, String razon) {
        return new SecurityEvent(
                UUID.randomUUID(),
                "LOGIN_FAILED",
                null,
                ip,
                Instant.now(),
                "identificador=" + identificador + ", razon=" + razon
        );
    }

    public static SecurityEvent logout(UUID usuarioId, String ip) {
        return new SecurityEvent(
                UUID.randomUUID(),
                "LOGOUT",
                usuarioId,
                ip,
                Instant.now(),
                null
        );
    }

    public static SecurityEvent tokenRefresh(UUID usuarioId, String ip) {
        return new SecurityEvent(
                UUID.randomUUID(),
                "TOKEN_REFRESH",
                usuarioId,
                ip,
                Instant.now(),
                null
        );
    }

    public static SecurityEvent cuentaBloqueada(UUID usuarioId, String ip) {
        return new SecurityEvent(
                UUID.randomUUID(),
                "ACCOUNT_LOCKED",
                usuarioId,
                ip,
                Instant.now(),
                "Cuenta bloqueada por intentos fallidos"
        );
    }

    public static SecurityEvent dashboardAcceso(UUID usuarioId, String ip, String rol) {
        return new SecurityEvent(
                UUID.randomUUID(),
                "DASHBOARD_ADMIN_ACCESS",
                usuarioId,
                ip,
                Instant.now(),
                "rol=" + rol
        );
    }
}
