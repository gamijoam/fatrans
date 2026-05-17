// 📁 com/tufondo/socios/infrastructure/persistence/entity/SolicitudRegistroEntity.java
package com.tufondo.socios.infrastructure.persistence.entity;

import com.tufondo.socios.domain.model.enums.EstadoCivil;
import com.tufondo.socios.domain.model.enums.EstadoSolicitud;
import com.tufondo.socios.domain.model.enums.Genero;
import com.tufondo.socios.domain.model.enums.TipoDocumento;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity JPA para Solicitud de Registro de Socio.
 */
/**
 * Los índices son gestionados por la migración Flyway V11
 * (`idx_solicitud_registro_estado_fecha` cubre estado + fecha_solicitud DESC).
 * No se declaran @Index aquí para evitar duplicación cuando se migre
 * `ddl-auto` de `update` a `validate`.
 */
@Entity
@Table(name = "solicitud_registro")
public class SolicitudRegistroEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nombre_completo", nullable = false, length = 100)
    private String nombreCompleto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false, length = 20)
    private TipoDocumento tipoDocumento;

    @Column(name = "cedula", nullable = false, unique = true, length = 20)
    private String cedula;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "genero", nullable = false, length = 20)
    private Genero genero;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_civil", nullable = false, length = 20)
    private EstadoCivil estadoCivil;

    @Column(name = "correo_electronico", nullable = false, unique = true, length = 255)
    private String correoElectronico;

    @Column(name = "telefono", nullable = false, length = 20)
    private String telefono;

    @Column(name = "empresa", nullable = false, length = 200)
    private String empresa;

    @Column(name = "rif_empresa", length = 20)
    private String rifEmpresa;

    @Column(name = "departamento", length = 100)
    private String departamento;

    @Column(name = "cargo", length = 100)
    private String cargo;

    @Column(name = "salario", precision = 18, scale = 2)
    private BigDecimal salario;

    @Column(name = "direccion_estado", length = 100)
    private String direccionEstado;

    @Column(name = "direccion_ciudad", length = 100)
    private String direccionCiudad;

    @Column(name = "direccion_municipio", length = 100)
    private String direccionMunicipio;

    @Column(name = "direccion_calle", length = 255)
    private String direccionCalle;

    @Column(name = "emergencia_nombre", length = 200)
    private String emergenciaNombre;

    @Column(name = "emergencia_telefono", length = 20)
    private String emergenciaTelefono;

    @Column(name = "emergencia_parentesco", length = 50)
    private String emergenciaParentesco;

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

    @Column(name = "acepta_terminos", nullable = false)
    private Boolean aceptaTerminos;

    @Column(name = "acepta_lopdp", nullable = false)
    private Boolean aceptaLopdp;

    /** Declaración jurada LOCDOFT (origen lícito de fondos). Issue #218 PR-B. */
    @Column(name = "acepta_locdoft", nullable = false)
    private Boolean aceptaLocdoft;

    // ---- Auditoría LOPDP / LOCDOFT (defensa legal Venezuela) ----
    @Column(name = "ip_registro", length = 45)
    private String ipRegistro;

    @Column(name = "user_agent_registro", length = 500)
    private String userAgentRegistro;

    @Column(name = "consent_lopdp_timestamp")
    private Instant consentLopdpTimestamp;

    @Column(name = "consent_locdoft_timestamp")
    private Instant consentLocdoftTimestamp;

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
    public Boolean getAceptaLocdoft() { return aceptaLocdoft; }
    public void setAceptaLocdoft(Boolean aceptaLocdoft) { this.aceptaLocdoft = aceptaLocdoft; }
    public String getIpRegistro() { return ipRegistro; }
    public void setIpRegistro(String ipRegistro) { this.ipRegistro = ipRegistro; }
    public String getUserAgentRegistro() { return userAgentRegistro; }
    public void setUserAgentRegistro(String userAgentRegistro) { this.userAgentRegistro = userAgentRegistro; }
    public Instant getConsentLopdpTimestamp() { return consentLopdpTimestamp; }
    public void setConsentLopdpTimestamp(Instant consentLopdpTimestamp) { this.consentLopdpTimestamp = consentLopdpTimestamp; }
    public Instant getConsentLocdoftTimestamp() { return consentLocdoftTimestamp; }
    public void setConsentLocdoftTimestamp(Instant consentLocdoftTimestamp) { this.consentLocdoftTimestamp = consentLocdoftTimestamp; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static SolicitudRegistroEntityBuilder builder() { return new SolicitudRegistroEntityBuilder(); }

    public static class SolicitudRegistroEntityBuilder {
        private SolicitudRegistroEntity e = new SolicitudRegistroEntity();
        public SolicitudRegistroEntityBuilder id(UUID v) { e.id = v; return this; }
        public SolicitudRegistroEntityBuilder nombreCompleto(String v) { e.nombreCompleto = v; return this; }
        public SolicitudRegistroEntityBuilder tipoDocumento(TipoDocumento v) { e.tipoDocumento = v; return this; }
        public SolicitudRegistroEntityBuilder cedula(String v) { e.cedula = v; return this; }
        public SolicitudRegistroEntityBuilder fechaNacimiento(LocalDate v) { e.fechaNacimiento = v; return this; }
        public SolicitudRegistroEntityBuilder genero(Genero v) { e.genero = v; return this; }
        public SolicitudRegistroEntityBuilder estadoCivil(EstadoCivil v) { e.estadoCivil = v; return this; }
        public SolicitudRegistroEntityBuilder correoElectronico(String v) { e.correoElectronico = v; return this; }
        public SolicitudRegistroEntityBuilder telefono(String v) { e.telefono = v; return this; }
        public SolicitudRegistroEntityBuilder empresa(String v) { e.empresa = v; return this; }
        public SolicitudRegistroEntityBuilder rifEmpresa(String v) { e.rifEmpresa = v; return this; }
        public SolicitudRegistroEntityBuilder departamento(String v) { e.departamento = v; return this; }
        public SolicitudRegistroEntityBuilder cargo(String v) { e.cargo = v; return this; }
        public SolicitudRegistroEntityBuilder salario(BigDecimal v) { e.salario = v; return this; }
        public SolicitudRegistroEntityBuilder direccionEstado(String v) { e.direccionEstado = v; return this; }
        public SolicitudRegistroEntityBuilder direccionCiudad(String v) { e.direccionCiudad = v; return this; }
        public SolicitudRegistroEntityBuilder direccionMunicipio(String v) { e.direccionMunicipio = v; return this; }
        public SolicitudRegistroEntityBuilder direccionCalle(String v) { e.direccionCalle = v; return this; }
        public SolicitudRegistroEntityBuilder emergenciaNombre(String v) { e.emergenciaNombre = v; return this; }
        public SolicitudRegistroEntityBuilder emergenciaTelefono(String v) { e.emergenciaTelefono = v; return this; }
        public SolicitudRegistroEntityBuilder emergenciaParentesco(String v) { e.emergenciaParentesco = v; return this; }
        public SolicitudRegistroEntityBuilder estado(EstadoSolicitud v) { e.estado = v; return this; }
        public SolicitudRegistroEntityBuilder fechaSolicitud(LocalDateTime v) { e.fechaSolicitud = v; return this; }
        public SolicitudRegistroEntityBuilder fechaRevision(LocalDateTime v) { e.fechaRevision = v; return this; }
        public SolicitudRegistroEntityBuilder revisadoPor(String v) { e.revisadoPor = v; return this; }
        public SolicitudRegistroEntityBuilder comentario(String v) { e.comentario = v; return this; }
        public SolicitudRegistroEntityBuilder motivoRechazo(String v) { e.motivoRechazo = v; return this; }
        public SolicitudRegistroEntityBuilder aceptaTerminos(Boolean v) { e.aceptaTerminos = v; return this; }
        public SolicitudRegistroEntityBuilder aceptaLopdp(Boolean v) { e.aceptaLopdp = v; return this; }
        public SolicitudRegistroEntityBuilder aceptaLocdoft(Boolean v) { e.aceptaLocdoft = v; return this; }
        public SolicitudRegistroEntityBuilder ipRegistro(String v) { e.ipRegistro = v; return this; }
        public SolicitudRegistroEntityBuilder userAgentRegistro(String v) { e.userAgentRegistro = v; return this; }
        public SolicitudRegistroEntityBuilder consentLopdpTimestamp(Instant v) { e.consentLopdpTimestamp = v; return this; }
        public SolicitudRegistroEntityBuilder consentLocdoftTimestamp(Instant v) { e.consentLocdoftTimestamp = v; return this; }
        public SolicitudRegistroEntityBuilder createdAt(LocalDateTime v) { e.createdAt = v; return this; }
        public SolicitudRegistroEntityBuilder updatedAt(LocalDateTime v) { e.updatedAt = v; return this; }
        public SolicitudRegistroEntity build() { return e; }
    }
}