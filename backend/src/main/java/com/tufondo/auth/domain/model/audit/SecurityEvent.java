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
    public static SecurityEventBuilder builder() {
        return new SecurityEventBuilder();
    }

    public static class SecurityEventBuilder {
        private UUID id = UUID.randomUUID();
        private String tipoEvento;
        private UUID usuarioId;
        private String ipAddress;
        private Instant timestamp = Instant.now();
        private String detalles;

        public SecurityEventBuilder id(UUID id) { this.id = id; return this; }
        public SecurityEventBuilder tipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; return this; }
        public SecurityEventBuilder usuarioId(UUID usuarioId) { this.usuarioId = usuarioId; return this; }
        public SecurityEventBuilder ipAddress(String ipAddress) { this.ipAddress = ipAddress; return this; }
        public SecurityEventBuilder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }
        public SecurityEventBuilder detalles(String detalles) { this.detalles = detalles; return this; }
        public SecurityEvent build() { return new SecurityEvent(id, tipoEvento, usuarioId, ipAddress, timestamp, detalles); }
    }
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

    public static SecurityEvent sesionesInvalidadas(UUID usuarioId, String ip, UUID invalidadoPor, int cantidad) {
        return new SecurityEvent(
                UUID.randomUUID(),
                "SESSIONS_INVALIDATED",
                usuarioId,
                ip,
                Instant.now(),
                "invalidado_por=" + invalidadoPor + ", cantidad=" + cantidad
        );
    }

    public static SecurityEvent sesionIndividualInvalidadas(UUID usuarioId, String ip, UUID invalidadoPor, String sesionId) {
        return new SecurityEvent(
                UUID.randomUUID(),
                "SESSION_INVALIDATED",
                usuarioId,
                ip,
                Instant.now(),
                "invalidado_por=" + invalidadoPor + ", sesion_id=" + sesionId
        );
    }
}
