// com/tufondo/beneficiarios/infrastructure/persistence/entity/BeneficiarioAuditEntity.java
package com.tufondo.beneficiarios.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad de auditoría para registrar cambios en beneficiarios.
 */
@Entity
@Table(name = "beneficiaries_audit",
    indexes = {
        @Index(name = "idx_audit_entidad_beneficiaries", columnList = "entidad_tipo, entidad_id"),
        @Index(name = "idx_audit_usuario_beneficiaries", columnList = "usuario_id, fecha_evento"),
        @Index(name = "idx_audit_fecha_beneficiaries", columnList = "fecha_evento")
    })
public class BeneficiarioAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "entidad_tipo", nullable = false, length = 50)
    private String entidadTipo;

    @Column(name = "entidad_id", nullable = false)
    private UUID entidadId;

    @Column(name = "accion", nullable = false, length = 30)
    private String accion;

    @Column(name = "usuario_id", nullable = false, length = 100)
    private String usuarioId;

    @Column(name = "rol_usuario", nullable = false, length = 30)
    private String rolUsuario;

    @Column(name = "ip_cliente", nullable = false, length = 45)
    private String ipCliente;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "datos_anteriores", columnDefinition = "jsonb")
    private String datosAnteriores;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "datos_nuevos", columnDefinition = "jsonb")
    private String datosNuevos;

    @Column(name = "fecha_evento", nullable = false)
    private Instant fechaEvento;

    public BeneficiarioAuditEntity() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID v) { this.id = v; }
    public String getEntidadTipo() { return entidadTipo; }
    public void setEntidadTipo(String v) { this.entidadTipo = v; }
    public UUID getEntidadId() { return entidadId; }
    public void setEntidadId(UUID v) { this.entidadId = v; }
    public String getAccion() { return accion; }
    public void setAccion(String v) { this.accion = v; }
    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String v) { this.usuarioId = v; }
    public String getRolUsuario() { return rolUsuario; }
    public void setRolUsuario(String v) { this.rolUsuario = v; }
    public String getIpCliente() { return ipCliente; }
    public void setIpCliente(String v) { this.ipCliente = v; }
    public String getDatosAnteriores() { return datosAnteriores; }
    public void setDatosAnteriores(String v) { this.datosAnteriores = v; }
    public String getDatosNuevos() { return datosNuevos; }
    public void setDatosNuevos(String v) { this.datosNuevos = v; }
    public Instant getFechaEvento() { return fechaEvento; }
    public void setFechaEvento(Instant v) { this.fechaEvento = v; }

    public static BeneficiarioAuditEntityBuilder builder() { return new BeneficiarioAuditEntityBuilder(); }

    public static class BeneficiarioAuditEntityBuilder {
        private BeneficiarioAuditEntity e = new BeneficiarioAuditEntity();
        public BeneficiarioAuditEntityBuilder id(UUID v) { e.id = v; return this; }
        public BeneficiarioAuditEntityBuilder entidadTipo(String v) { e.entidadTipo = v; return this; }
        public BeneficiarioAuditEntityBuilder entidadId(UUID v) { e.entidadId = v; return this; }
        public BeneficiarioAuditEntityBuilder accion(String v) { e.accion = v; return this; }
        public BeneficiarioAuditEntityBuilder usuarioId(String v) { e.usuarioId = v; return this; }
        public BeneficiarioAuditEntityBuilder rolUsuario(String v) { e.rolUsuario = v; return this; }
        public BeneficiarioAuditEntityBuilder ipCliente(String v) { e.ipCliente = v; return this; }
        public BeneficiarioAuditEntityBuilder datosAnteriores(String v) { e.datosAnteriores = v; return this; }
        public BeneficiarioAuditEntityBuilder datosNuevos(String v) { e.datosNuevos = v; return this; }
        public BeneficiarioAuditEntityBuilder fechaEvento(Instant v) { e.fechaEvento = v; return this; }
        public BeneficiarioAuditEntity build() { return e; }
    }
}