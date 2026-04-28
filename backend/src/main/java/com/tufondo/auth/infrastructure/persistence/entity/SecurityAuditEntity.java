package com.tufondo.auth.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "security_audit_log")
public class SecurityAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tipo_evento", nullable = false, length = 50)
    private String tipoEvento;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "detalles", columnDefinition = "TEXT")
    private String detalles;

    @Column(name = "entity_type", length = 50)
    private String entityType;

    @Column(name = "entity_id", length = 100)
    private String entityId;

    @Column(name = "action", length = 50)
    private String action;

    public SecurityAuditEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }
    public UUID getUsuarioId() { return usuarioId; }
    public void setUsuarioId(UUID usuarioId) { this.usuarioId = usuarioId; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public String getDetalles() { return detalles; }
    public void setDetalles(String detalles) { this.detalles = detalles; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public static SecurityAuditEntityBuilder builder() {
        return new SecurityAuditEntityBuilder();
    }

    public static class SecurityAuditEntityBuilder {
        private final SecurityAuditEntity entity = new SecurityAuditEntity();

        public SecurityAuditEntityBuilder id(UUID id) { entity.id = id; return this; }
        public SecurityAuditEntityBuilder tipoEvento(String tipoEvento) { entity.tipoEvento = tipoEvento; return this; }
        public SecurityAuditEntityBuilder usuarioId(UUID usuarioId) { entity.usuarioId = usuarioId; return this; }
        public SecurityAuditEntityBuilder ipAddress(String ipAddress) { entity.ipAddress = ipAddress; return this; }
        public SecurityAuditEntityBuilder timestamp(Instant timestamp) { entity.timestamp = timestamp; return this; }
        public SecurityAuditEntityBuilder detalles(String detalles) { entity.detalles = detalles; return this; }
        public SecurityAuditEntityBuilder entityType(String entityType) { entity.entityType = entityType; return this; }
        public SecurityAuditEntityBuilder entityId(String entityId) { entity.entityId = entityId; return this; }
        public SecurityAuditEntityBuilder action(String action) { entity.action = action; return this; }
        public SecurityAuditEntity build() { return entity; }
    }
}