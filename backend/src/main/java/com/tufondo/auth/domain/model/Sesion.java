package com.tufondo.auth.domain.model;

import com.tufondo.auth.domain.model.enums.TipoToken;
import java.time.Instant;
import java.util.UUID;

/**
 * Modelo de dominio para representar una sesión de usuario.
 * Mantiene los tokens de acceso y actualización asociados.
 */
public final class Sesion {

    private final UUID id;
    private final UUID usuarioId;
    private final String refreshToken;
    private final Instant accessTokenExpiracion;
    private final Instant refreshTokenExpiracion;
    private final boolean activo;
    private final TipoToken tipoToken;
    private final Instant fechaCreacion;
    private final Instant ultimaActividad;

    public Sesion(
            UUID id,
            UUID usuarioId,
            String refreshToken,
            Instant accessTokenExpiracion,
            Instant refreshTokenExpiracion,
            boolean activo,
            TipoToken tipoToken,
            Instant fechaCreacion,
            Instant ultimaActividad
    ) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.refreshToken = refreshToken;
        this.accessTokenExpiracion = accessTokenExpiracion;
        this.refreshTokenExpiracion = refreshTokenExpiracion;
        this.activo = activo;
        this.tipoToken = tipoToken;
        this.fechaCreacion = fechaCreacion;
        this.ultimaActividad = ultimaActividad;
    }

    public static Sesion desdeParametros(
            UUID id,
            UUID usuarioId,
            String refreshToken,
            Instant accessTokenExpiracion,
            Instant refreshTokenExpiracion,
            boolean activo,
            TipoToken tipoToken,
            Instant fechaCreacion,
            Instant ultimaActividad
    ) {
        return new Sesion(id, usuarioId, refreshToken, accessTokenExpiracion,
                refreshTokenExpiracion, activo, tipoToken, fechaCreacion, ultimaActividad);
    }

    public Sesion conActivo(boolean activo) {
        return new Sesion(id, usuarioId, refreshToken, accessTokenExpiracion,
                refreshTokenExpiracion, activo, tipoToken, fechaCreacion, ultimaActividad);
    }

    public Sesion conUltimaActividad(Instant ultimaActividad) {
        return new Sesion(id, usuarioId, refreshToken, accessTokenExpiracion,
                refreshTokenExpiracion, activo, tipoToken, fechaCreacion, ultimaActividad);
    }

    /**
     * Verifica si la sesión ha expirado.
     *
     * @return true si el refresh token ha expirado
     */
    public boolean estaExpirado() {
        return !activo || Instant.now().isAfter(refreshTokenExpiracion);
    }

    /**
     * Verifica si el access token ha expirado.
     *
     * @return true si el access token ha expirado
     */
    public boolean accessTokenExpirado() {
        return !activo || Instant.now().isAfter(accessTokenExpiracion);
    }

    public boolean activa() { return activo; }
    public TipoToken tipoToken() { return tipoToken; }
    public Instant fechaCreacion() { return fechaCreacion; }
    public Instant ultimaActividad() { return ultimaActividad; }

    // Getters
    public UUID id() { return id; }
    public UUID usuarioId() { return usuarioId; }
    public String refreshToken() { return refreshToken; }
    public Instant accessTokenExpiracion() { return accessTokenExpiracion; }
    public Instant refreshTokenExpiracion() { return refreshTokenExpiracion; }
}
