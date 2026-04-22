package com.tufondo.auth.infrastructure.persistence.entity;

import com.tufondo.auth.domain.model.PasswordResetToken;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Entidad JPA para token de recuperación de contraseña.
 */
@Entity
@Table(name = "password_reset_token")
public class PasswordResetTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "token", nullable = false, unique = true, length = 100)
    private String token;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used", nullable = false)
    private boolean used;

    public PasswordResetTokenEntity() {}

    public static PasswordResetTokenEntity desdeDominio(PasswordResetToken domain) {
        PasswordResetTokenEntity entity = new PasswordResetTokenEntity();
        entity.id = domain.id();
        entity.token = domain.token();
        entity.usuarioId = domain.usuarioId();
        entity.createdAt = domain.createdAt();
        entity.expiresAt = domain.expiresAt();
        entity.used = domain.used();
        return entity;
    }

    public PasswordResetToken aDominio() {
        return PasswordResetToken.desdeParametros(
            id, usuarioId, token, createdAt, expiresAt, used
        );
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public UUID getUsuarioId() { return usuarioId; }
    public void setUsuarioId(UUID usuarioId) { this.usuarioId = usuarioId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }
}
