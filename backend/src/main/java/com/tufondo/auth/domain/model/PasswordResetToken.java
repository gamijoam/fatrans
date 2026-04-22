package com.tufondo.auth.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Modelo de dominio para token de recuperación de contraseña.
 */
public final class PasswordResetToken {

    private final UUID id;
    private final UUID usuarioId;
    private final String token;
    private final Instant createdAt;
    private final Instant expiresAt;
    private final boolean used;

    private PasswordResetToken(UUID id, UUID usuarioId, String token,
                                Instant createdAt, Instant expiresAt, boolean used) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.token = token;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.used = used;
    }

    /**
     * Crea un nuevo token de recuperación de contraseña.
     * El token expira en 1 hora por defecto.
     */
    public static PasswordResetToken crear(UUID usuarioId) {
        UUID id = UUID.randomUUID();
        String token = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(3600); // 1 hora

        return new PasswordResetToken(id, usuarioId, token, now, expiresAt, false);
    }

    /**
     * Crea un token desde parámetros (para reconstruir desde la DB).
     */
    public static PasswordResetToken desdeParametros(UUID id, UUID usuarioId, String token,
                                                      Instant createdAt, Instant expiresAt, boolean used) {
        return new PasswordResetToken(id, usuarioId, token, createdAt, expiresAt, used);
    }

    /**
     * Marca el token como usado después de successfully reset password.
     */
    public PasswordResetToken marcarComoUsado() {
        return new PasswordResetToken(id, usuarioId, token, createdAt, expiresAt, true);
    }

    /**
     * Verifica si el token ha expirado.
     */
    public boolean estaExpirado() {
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * Verifica si el token es válido (no usado y no expirado).
     */
    public boolean esValido() {
        return !used && !estaExpirado();
    }

    public UUID id() { return id; }
    public UUID usuarioId() { return usuarioId; }
    public String token() { return token; }
    public Instant createdAt() { return createdAt; }
    public Instant expiresAt() { return expiresAt; }
    public boolean used() { return used; }
}
