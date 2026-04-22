// 📁 com/tufondo/socios/infrastructure/persistence/entity/SolicitudRegistroEntity.java
package com.tufondo.socios.infrastructure.persistence.entity;

import com.tufondo.socios.domain.model.enums.EstadoSolicitud;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity JPA para Solicitud de Registro de Socio.
 */
@Entity
@Table(name = "solicitud_registro", indexes = {
    @Index(name = "idx_solicitud_estado", columnList = "estado"),
    @Index(name = "idx_solicitud_fecha", columnList = "fecha_solicitud DESC")
})
public class SolicitudRegistroEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nombre_completo", nullable = false, length = 100)
    private String nombreCompleto;

    @Column(name = "cedula", nullable = false, unique = true, length = 20)
    private String cedula;

    @Column(name = "correo_electronico", nullable = false, unique = true, length = 255)
    private String correoElectronico;

    @Column(name = "telefono", nullable = false, length = 20)
    private String telefono;

    @Column(name = "empresa", nullable = false, length = 200)
    private String empresa;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoSolicitud estado;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_revision")
    private LocalDateTime fechaRevision;

    @Column(name = "revisado_por", length = 100)
    private String revisadoPor;

    @Column(name = "comentario", length = 500)
    private String comentario;

    @Column(name = "motivo_rechazo", columnDefinition = "TEXT")
    private String motivoRechazo;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public SolicitudRegistroEntity() {}

    // Getters y Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }
    public String getCorreoElectronico() { return correoElectronico; }
    public void setCorreoElectronico(String correoElectronico) { this.correoElectronico = correoElectronico; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }
    public EstadoSolicitud getEstado() { return estado; }
    public void setEstado(EstadoSolicitud estado) { this.estado = estado; }
    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }
    public LocalDateTime getFechaRevision() { return fechaRevision; }
    public void setFechaRevision(LocalDateTime fechaRevision) { this.fechaRevision = fechaRevision; }
    public String getRevisadoPor() { return revisadoPor; }
    public void setRevisadoPor(String revisadoPor) { this.revisadoPor = revisadoPor; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static SolicitudRegistroEntityBuilder builder() { return new SolicitudRegistroEntityBuilder(); }

    public static class SolicitudRegistroEntityBuilder {
        private SolicitudRegistroEntity e = new SolicitudRegistroEntity();
        public SolicitudRegistroEntityBuilder id(UUID v) { e.id = v; return this; }
        public SolicitudRegistroEntityBuilder nombreCompleto(String v) { e.nombreCompleto = v; return this; }
        public SolicitudRegistroEntityBuilder cedula(String v) { e.cedula = v; return this; }
        public SolicitudRegistroEntityBuilder correoElectronico(String v) { e.correoElectronico = v; return this; }
        public SolicitudRegistroEntityBuilder telefono(String v) { e.telefono = v; return this; }
        public SolicitudRegistroEntityBuilder empresa(String v) { e.empresa = v; return this; }
        public SolicitudRegistroEntityBuilder estado(EstadoSolicitud v) { e.estado = v; return this; }
        public SolicitudRegistroEntityBuilder fechaSolicitud(LocalDateTime v) { e.fechaSolicitud = v; return this; }
        public SolicitudRegistroEntityBuilder fechaRevision(LocalDateTime v) { e.fechaRevision = v; return this; }
        public SolicitudRegistroEntityBuilder revisadoPor(String v) { e.revisadoPor = v; return this; }
        public SolicitudRegistroEntityBuilder comentario(String v) { e.comentario = v; return this; }
        public SolicitudRegistroEntityBuilder motivoRechazo(String v) { e.motivoRechazo = v; return this; }
        public SolicitudRegistroEntityBuilder createdAt(LocalDateTime v) { e.createdAt = v; return this; }
        public SolicitudRegistroEntityBuilder updatedAt(LocalDateTime v) { e.updatedAt = v; return this; }
        public SolicitudRegistroEntity build() { return e; }
    }
}