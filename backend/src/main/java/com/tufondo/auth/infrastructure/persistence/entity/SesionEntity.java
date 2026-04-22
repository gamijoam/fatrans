package com.tufondo.auth.infrastructure.persistence.entity;

import com.tufondo.auth.domain.model.Sesion;
import com.tufondo.auth.domain.model.enums.TipoToken;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sesiones")
public class SesionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "refresh_token_hash", nullable = false, unique = true)
    private String refreshTokenHash;

    @Column(name = "access_token_expiracion", nullable = false)
    private Instant accessTokenExpiracion;

    @Column(name = "refresh_token_expiracion", nullable = false)
    private Instant refreshTokenExpiracion;

    @Column(nullable = false)
    private boolean activo;

    @Column(name = "tipo_token", nullable = false)
    private String tipoToken;

    @Column(name = "fecha_creacion", nullable = false)
    private Instant fechaCreacion;

    @Column(name = "ultima_actividad")
    private Instant ultimaActividad;

    public SesionEntity() {}

    public SesionEntity(
            UUID id,
            UUID usuarioId,
            String refreshTokenHash,
            Instant accessTokenExpiracion,
            Instant refreshTokenExpiracion,
            boolean activo,
            String tipoToken
    ) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.refreshTokenHash = refreshTokenHash;
        this.accessTokenExpiracion = accessTokenExpiracion;
        this.refreshTokenExpiracion = refreshTokenExpiracion;
        this.activo = activo;
        this.tipoToken = tipoToken;
        this.fechaCreacion = Instant.now();
        this.ultimaActividad = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUsuarioId() { return usuarioId; }
    public void setUsuarioId(UUID usuarioId) { this.usuarioId = usuarioId; }

    public String getRefreshTokenHash() { return refreshTokenHash; }
    public void setRefreshTokenHash(String refreshTokenHash) { this.refreshTokenHash = refreshTokenHash; }

    public Instant getAccessTokenExpiracion() { return accessTokenExpiracion; }
    public void setAccessTokenExpiracion(Instant accessTokenExpiracion) {
        this.accessTokenExpiracion = accessTokenExpiracion;
    }

    public Instant getRefreshTokenExpiracion() { return refreshTokenExpiracion; }
    public void setRefreshTokenExpiracion(Instant refreshTokenExpiracion) {
        this.refreshTokenExpiracion = refreshTokenExpiracion;
    }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public String getTipoToken() { return tipoToken; }
    public void setTipoToken(String tipoToken) { this.tipoToken = tipoToken; }

    public Instant getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Instant fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Instant getUltimaActividad() { return ultimaActividad; }
    public void setUltimaActividad(Instant ultimaActividad) { this.ultimaActividad = ultimaActividad; }

    public Sesion aDominio() {
        TipoToken tipo = TipoToken.valueOf(this.tipoToken);
        return Sesion.desdeParametros(
                this.id,
                this.usuarioId,
                this.refreshTokenHash,
                this.accessTokenExpiracion,
                this.refreshTokenExpiracion,
                this.activo,
                tipo,
                this.fechaCreacion,
                this.ultimaActividad
        );
    }

    public static SesionEntity desdeDominio(Sesion sesion) {
        SesionEntity entity = new SesionEntity();
        entity.setId(sesion.id());
        entity.setUsuarioId(sesion.usuarioId());
        entity.setRefreshTokenHash(sesion.refreshToken());
        entity.setAccessTokenExpiracion(sesion.accessTokenExpiracion());
        entity.setRefreshTokenExpiracion(sesion.refreshTokenExpiracion());
        entity.setActivo(sesion.activa());
        entity.setTipoToken(sesion.tipoToken().name());
        entity.setFechaCreacion(sesion.fechaCreacion());
        entity.setUltimaActividad(sesion.ultimaActividad());
        return entity;
    }
}
