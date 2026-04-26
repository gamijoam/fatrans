package com.tufondo.auth.infrastructure.persistence.entity;

import com.tufondo.socios.domain.model.enums.TipoVerificacion;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "verificacion_token", indexes = {
    @Index(name = "idx_verificacion_token", columnList = "token", unique = true),
    @Index(name = "idx_verificacion_usuario", columnList = "usuario_id"),
    @Index(name = "idx_verificacion_expira", columnList = "expires_at")
})
public class VerificacionTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "token", nullable = false, unique = true, length = 100)
    private String token;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoVerificacion tipo;

    @Column(name = "valor", length = 255)
    private String valor;

    @Column(name = "codigo", length = 10)
    private String codigo;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used", nullable = false)
    private boolean used;

    @Column(name = "intentos", nullable = false)
    private int intentos;

    public VerificacionTokenEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public UUID getUsuarioId() { return usuarioId; }
    public void setUsuarioId(UUID usuarioId) { this.usuarioId = usuarioId; }
    public TipoVerificacion getTipo() { return tipo; }
    public void setTipo(TipoVerificacion tipo) { this.tipo = tipo; }
    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }
    public int getIntentos() { return intentos; }
    public void setIntentos(int intentos) { this.intentos = intentos; }

    public static VerificacionTokenEntityBuilder builder() {
        return new VerificacionTokenEntityBuilder();
    }

    public static class VerificacionTokenEntityBuilder {
        private VerificacionTokenEntity e = new VerificacionTokenEntity();
        public VerificacionTokenEntityBuilder id(UUID v) { e.id = v; return this; }
        public VerificacionTokenEntityBuilder token(String v) { e.token = v; return this; }
        public VerificacionTokenEntityBuilder usuarioId(UUID v) { e.usuarioId = v; return this; }
        public VerificacionTokenEntityBuilder tipo(TipoVerificacion v) { e.tipo = v; return this; }
        public VerificacionTokenEntityBuilder valor(String v) { e.valor = v; return this; }
        public VerificacionTokenEntityBuilder codigo(String v) { e.codigo = v; return this; }
        public VerificacionTokenEntityBuilder ipAddress(String v) { e.ipAddress = v; return this; }
        public VerificacionTokenEntityBuilder userAgent(String v) { e.userAgent = v; return this; }
        public VerificacionTokenEntityBuilder createdAt(Instant v) { e.createdAt = v; return this; }
        public VerificacionTokenEntityBuilder expiresAt(Instant v) { e.expiresAt = v; return this; }
        public VerificacionTokenEntityBuilder used(boolean v) { e.used = v; return this; }
        public VerificacionTokenEntityBuilder intentos(int v) { e.intentos = v; return this; }
        public VerificacionTokenEntity build() { return e; }
    }
}