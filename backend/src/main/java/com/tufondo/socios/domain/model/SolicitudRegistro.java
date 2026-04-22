// 📁 com/tufondo/socios/domain/model/SolicitudRegistro.java
package com.tufondo.socios.domain.model;

import com.tufondo.socios.domain.model.enums.EstadoSolicitud;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modelo de dominio para Solicitud de Registro de Socio.
 * Representa la solicitud inicial creada por un usuario que desea registrarse.
 */
public class SolicitudRegistro {
    
    private UUID id;
    private String nombreCompleto;
    private String cedula;
    private String correoElectronico;
    private String telefono;
    private String empresa;
    private EstadoSolicitud estado;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaRevision;
    private String revisadoPor;
    private String comentario;
    private String motivoRechazo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SolicitudRegistro() {}

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

    /**
     * Aprueba la solicitud y registra la información del administrador.
     * @param adminId ID del administrador que aprueba
     * @param comentario Comentario opcional del administrador
     */
    public void aprobar(String adminId, String comentario) {
        if (this.estado != EstadoSolicitud.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden aprobar solicitudes pendientes");
        }
        this.estado = EstadoSolicitud.APROBADA;
        this.fechaRevision = LocalDateTime.now();
        this.revisadoPor = adminId;
        this.comentario = comentario;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Rechaza la solicitud y registra la información del administrador.
     * @param adminId ID del administrador que rechaza
     * @param motivo Motivo del rechazo (obligatorio)
     */
    public void rechazar(String adminId, String motivo) {
        if (this.estado != EstadoSolicitud.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden rechazar solicitudes pendientes");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo de rechazo es obligatorio");
        }
        this.estado = EstadoSolicitud.RECHAZADA;
        this.fechaRevision = LocalDateTime.now();
        this.revisadoPor = adminId;
        this.motivoRechazo = motivo;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Verifica si la solicitud está pendiente.
     */
    public boolean estaPendiente() {
        return this.estado == EstadoSolicitud.PENDIENTE;
    }

    public static SolicitudRegistroBuilder builder() { return new SolicitudRegistroBuilder(); }

    public static class SolicitudRegistroBuilder {
        private SolicitudRegistro s = new SolicitudRegistro();
        public SolicitudRegistroBuilder id(UUID v) { s.id = v; return this; }
        public SolicitudRegistroBuilder nombreCompleto(String v) { s.nombreCompleto = v; return this; }
        public SolicitudRegistroBuilder cedula(String v) { s.cedula = v; return this; }
        public SolicitudRegistroBuilder correoElectronico(String v) { s.correoElectronico = v; return this; }
        public SolicitudRegistroBuilder telefono(String v) { s.telefono = v; return this; }
        public SolicitudRegistroBuilder empresa(String v) { s.empresa = v; return this; }
        public SolicitudRegistroBuilder estado(EstadoSolicitud v) { s.estado = v; return this; }
        public SolicitudRegistroBuilder fechaSolicitud(LocalDateTime v) { s.fechaSolicitud = v; return this; }
        public SolicitudRegistroBuilder fechaRevision(LocalDateTime v) { s.fechaRevision = v; return this; }
        public SolicitudRegistroBuilder revisadoPor(String v) { s.revisadoPor = v; return this; }
        public SolicitudRegistroBuilder comentario(String v) { s.comentario = v; return this; }
        public SolicitudRegistroBuilder motivoRechazo(String v) { s.motivoRechazo = v; return this; }
        public SolicitudRegistroBuilder createdAt(LocalDateTime v) { s.createdAt = v; return this; }
        public SolicitudRegistroBuilder updatedAt(LocalDateTime v) { s.updatedAt = v; return this; }
        public SolicitudRegistro build() { return s; }
    }
}