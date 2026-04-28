// 📁 com/tufondo/socios/domain/model/SolicitudRegistro.java
package com.tufondo.socios.domain.model;

import com.tufondo.socios.domain.model.enums.EstadoCivil;
import com.tufondo.socios.domain.model.enums.EstadoSolicitud;
import com.tufondo.socios.domain.model.enums.Genero;
import com.tufondo.socios.domain.model.enums.TipoDocumento;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modelo de dominio para Solicitud de Registro de Socio.
 * Representa la solicitud inicial creada por un usuario que desea registrarse.
 */
public class SolicitudRegistro {
    
    private UUID id;
    private String nombreCompleto;
    private TipoDocumento tipoDocumento;
    private String cedula;
    private LocalDate fechaNacimiento;
    private Genero genero;
    private EstadoCivil estadoCivil;
    private String correoElectronico;
    private String telefono;
    private String empresa;
    private String rifEmpresa;
    private String departamento;
    private String cargo;
    private BigDecimal salario;
    private String direccionEstado;
    private String direccionCiudad;
    private String direccionMunicipio;
    private String direccionCalle;
    private String emergenciaNombre;
    private String emergenciaTelefono;
    private String emergenciaParentesco;
    private EstadoSolicitud estado;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaRevision;
    private String revisadoPor;
    private String comentario;
    private String motivoRechazo;
    private Boolean aceptaTerminos;
    private Boolean aceptaLopdp;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SolicitudRegistro() {}

    // Getters y Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public TipoDocumento getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(TipoDocumento tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public Genero getGenero() { return genero; }
    public void setGenero(Genero genero) { this.genero = genero; }
    public EstadoCivil getEstadoCivil() { return estadoCivil; }
    public void setEstadoCivil(EstadoCivil estadoCivil) { this.estadoCivil = estadoCivil; }
    public String getCorreoElectronico() { return correoElectronico; }
    public void setCorreoElectronico(String correoElectronico) { this.correoElectronico = correoElectronico; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }
    public String getRifEmpresa() { return rifEmpresa; }
    public void setRifEmpresa(String rifEmpresa) { this.rifEmpresa = rifEmpresa; }
    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }
    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    public BigDecimal getSalario() { return salario; }
    public void setSalario(BigDecimal salario) { this.salario = salario; }
    public String getDireccionEstado() { return direccionEstado; }
    public void setDireccionEstado(String direccionEstado) { this.direccionEstado = direccionEstado; }
    public String getDireccionCiudad() { return direccionCiudad; }
    public void setDireccionCiudad(String direccionCiudad) { this.direccionCiudad = direccionCiudad; }
    public String getDireccionMunicipio() { return direccionMunicipio; }
    public void setDireccionMunicipio(String direccionMunicipio) { this.direccionMunicipio = direccionMunicipio; }
    public String getDireccionCalle() { return direccionCalle; }
    public void setDireccionCalle(String direccionCalle) { this.direccionCalle = direccionCalle; }
    public String getEmergenciaNombre() { return emergenciaNombre; }
    public void setEmergenciaNombre(String emergenciaNombre) { this.emergenciaNombre = emergenciaNombre; }
    public String getEmergenciaTelefono() { return emergenciaTelefono; }
    public void setEmergenciaTelefono(String emergenciaTelefono) { this.emergenciaTelefono = emergenciaTelefono; }
    public String getEmergenciaParentesco() { return emergenciaParentesco; }
    public void setEmergenciaParentesco(String emergenciaParentesco) { this.emergenciaParentesco = emergenciaParentesco; }
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
    public Boolean getAceptaTerminos() { return aceptaTerminos; }
    public void setAceptaTerminos(Boolean aceptaTerminos) { this.aceptaTerminos = aceptaTerminos; }
    public Boolean getAceptaLopdp() { return aceptaLopdp; }
    public void setAceptaLopdp(Boolean aceptaLopdp) { this.aceptaLopdp = aceptaLopdp; }
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
        public SolicitudRegistroBuilder tipoDocumento(TipoDocumento v) { s.tipoDocumento = v; return this; }
        public SolicitudRegistroBuilder cedula(String v) { s.cedula = v; return this; }
        public SolicitudRegistroBuilder fechaNacimiento(LocalDate v) { s.fechaNacimiento = v; return this; }
        public SolicitudRegistroBuilder genero(Genero v) { s.genero = v; return this; }
        public SolicitudRegistroBuilder estadoCivil(EstadoCivil v) { s.estadoCivil = v; return this; }
        public SolicitudRegistroBuilder correoElectronico(String v) { s.correoElectronico = v; return this; }
        public SolicitudRegistroBuilder telefono(String v) { s.telefono = v; return this; }
        public SolicitudRegistroBuilder empresa(String v) { s.empresa = v; return this; }
        public SolicitudRegistroBuilder rifEmpresa(String v) { s.rifEmpresa = v; return this; }
        public SolicitudRegistroBuilder departamento(String v) { s.departamento = v; return this; }
        public SolicitudRegistroBuilder cargo(String v) { s.cargo = v; return this; }
        public SolicitudRegistroBuilder salario(BigDecimal v) { s.salario = v; return this; }
        public SolicitudRegistroBuilder direccionEstado(String v) { s.direccionEstado = v; return this; }
        public SolicitudRegistroBuilder direccionCiudad(String v) { s.direccionCiudad = v; return this; }
        public SolicitudRegistroBuilder direccionMunicipio(String v) { s.direccionMunicipio = v; return this; }
        public SolicitudRegistroBuilder direccionCalle(String v) { s.direccionCalle = v; return this; }
        public SolicitudRegistroBuilder emergenciaNombre(String v) { s.emergenciaNombre = v; return this; }
        public SolicitudRegistroBuilder emergenciaTelefono(String v) { s.emergenciaTelefono = v; return this; }
        public SolicitudRegistroBuilder emergenciaParentesco(String v) { s.emergenciaParentesco = v; return this; }
        public SolicitudRegistroBuilder estado(EstadoSolicitud v) { s.estado = v; return this; }
        public SolicitudRegistroBuilder fechaSolicitud(LocalDateTime v) { s.fechaSolicitud = v; return this; }
        public SolicitudRegistroBuilder fechaRevision(LocalDateTime v) { s.fechaRevision = v; return this; }
        public SolicitudRegistroBuilder revisadoPor(String v) { s.revisadoPor = v; return this; }
        public SolicitudRegistroBuilder comentario(String v) { s.comentario = v; return this; }
        public SolicitudRegistroBuilder motivoRechazo(String v) { s.motivoRechazo = v; return this; }
        public SolicitudRegistroBuilder aceptaTerminos(Boolean v) { s.aceptaTerminos = v; return this; }
        public SolicitudRegistroBuilder aceptaLopdp(Boolean v) { s.aceptaLopdp = v; return this; }
        public SolicitudRegistroBuilder createdAt(LocalDateTime v) { s.createdAt = v; return this; }
        public SolicitudRegistroBuilder updatedAt(LocalDateTime v) { s.updatedAt = v; return this; }
        public SolicitudRegistro build() { return s; }
    }
}