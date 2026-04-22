// com/tufondo/socios/infrastructure/persistence/entity/SocioEntity.java
package com.tufondo.socios.infrastructure.persistence.entity;

import com.tufondo.socios.domain.model.enums.EstadoCivil;
import com.tufondo.socios.domain.model.enums.EstadoSocio;
import com.tufondo.socios.domain.model.enums.Genero;
import com.tufondo.socios.domain.model.enums.TipoContrato;
import com.tufondo.socios.domain.model.enums.TipoDocumento;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "socios")
public class SocioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "numero_socio", unique = true, nullable = false)
    private String numeroSocio;

    @Column(name = "primer_nombre", nullable = false)
    private String primerNombre;
    @Column(name = "segundo_nombre")
    private String segundoNombre;
    @Column(name = "primer_apellido", nullable = false)
    private String primerApellido;
    @Column(name = "segundo_apellido")
    private String segundoApellido;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Genero genero;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_civil", nullable = false)
    private EstadoCivil estadoCivil;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false)
    private TipoDocumento tipoDocumento;

    @Column(name = "numero_documento", nullable = false, unique = true)
    private String numeroDocumento;

    @Column(name = "correo_electronico", nullable = false, unique = true)
    private String correoElectronico;

    @Column(name = "telefono_principal")
    private String telefonoPrincipal;

    @Column(name = "telefono_secundario")
    private String telefonoSecundario;

    @Embedded
    @jakarta.persistence.AttributeOverrides({
        @jakarta.persistence.AttributeOverride(name = "calle", column = @Column(name = "residencia_calle")),
        @jakarta.persistence.AttributeOverride(name = "numero", column = @Column(name = "residencia_numero")),
        @jakarta.persistence.AttributeOverride(name = "colonia", column = @Column(name = "residencia_colonia")),
        @jakarta.persistence.AttributeOverride(name = "ciudad", column = @Column(name = "residencia_ciudad")),
        @jakarta.persistence.AttributeOverride(name = "estado", column = @Column(name = "residencia_estado")),
        @jakarta.persistence.AttributeOverride(name = "codigoPostal", column = @Column(name = "residencia_cp")),
        @jakarta.persistence.AttributeOverride(name = "pais", column = @Column(name = "residencia_pais"))
    })
    private DireccionEmbed direccionResidencia;

    @Embedded
    @jakarta.persistence.AttributeOverrides({
        @jakarta.persistence.AttributeOverride(name = "calle", column = @Column(name = "laboral_calle")),
        @jakarta.persistence.AttributeOverride(name = "numero", column = @Column(name = "laboral_numero")),
        @jakarta.persistence.AttributeOverride(name = "colonia", column = @Column(name = "laboral_colonia")),
        @jakarta.persistence.AttributeOverride(name = "ciudad", column = @Column(name = "laboral_ciudad")),
        @jakarta.persistence.AttributeOverride(name = "estado", column = @Column(name = "laboral_estado")),
        @jakarta.persistence.AttributeOverride(name = "codigoPostal", column = @Column(name = "laboral_cp")),
        @jakarta.persistence.AttributeOverride(name = "pais", column = @Column(name = "laboral_pais"))
    })
    private DireccionEmbed direccionLaboral;

    @Column(name = "empresa")
    private String empresa;

    @Column(name = "departamento")
    private String departamento;

    @Column(name = "cargo")
    private String cargo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_contrato")
    private TipoContrato tipoContrato;

    @Column(precision = 19, scale = 4)
    private java.math.BigDecimal salario;

    @Column(name = "monto_ahorro", precision = 19, scale = 4)
    private java.math.BigDecimal montoAhorro;

    @Column(name = "numero_cuenta_nomina")
    private String numeroCuentaNomina;

    @Column(name = "banco_nomina")
    private String bancoNomina;

    @Embedded
    private ContactoEmergenciaEmbed contactoEmergencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoSocio estado;

    @Column(name = "fecha_ingreso")
    private LocalDate fechaIngreso;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "fecha_activacion")
    private LocalDateTime fechaActivacion;

    @Column(name = "fecha_desactivacion")
    private LocalDateTime fechaDesactivacion;

    @Column(name = "motivo_desactivacion")
    private String motivoDesactivacion;

    @jakarta.persistence.ElementCollection
    @jakarta.persistence.CollectionTable(name = "socio_roles", joinColumns = @jakarta.persistence.JoinColumn(name = "socio_id"))
    @Column(name = "rol")
    private java.util.Set<String> roles;

    public SocioEntity() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getNumeroSocio() { return numeroSocio; }
    public void setNumeroSocio(String v) { this.numeroSocio = v; }
    public String getPrimerNombre() { return primerNombre; }
    public void setPrimerNombre(String v) { this.primerNombre = v; }
    public String getSegundoNombre() { return segundoNombre; }
    public void setSegundoNombre(String v) { this.segundoNombre = v; }
    public String getPrimerApellido() { return primerApellido; }
    public void setPrimerApellido(String v) { this.primerApellido = v; }
    public String getSegundoApellido() { return segundoApellido; }
    public void setSegundoApellido(String v) { this.segundoApellido = v; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate v) { this.fechaNacimiento = v; }
    public Genero getGenero() { return genero; }
    public void setGenero(Genero v) { this.genero = v; }
    public EstadoCivil getEstadoCivil() { return estadoCivil; }
    public void setEstadoCivil(EstadoCivil v) { this.estadoCivil = v; }
    public TipoDocumento getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(TipoDocumento v) { this.tipoDocumento = v; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String v) { this.numeroDocumento = v; }
    public String getCorreoElectronico() { return correoElectronico; }
    public void setCorreoElectronico(String v) { this.correoElectronico = v; }
    public String getTelefonoPrincipal() { return telefonoPrincipal; }
    public void setTelefonoPrincipal(String v) { this.telefonoPrincipal = v; }
    public String getTelefonoSecundario() { return telefonoSecundario; }
    public void setTelefonoSecundario(String v) { this.telefonoSecundario = v; }
    public DireccionEmbed getDireccionResidencia() { return direccionResidencia; }
    public void setDireccionResidencia(DireccionEmbed v) { this.direccionResidencia = v; }
    public DireccionEmbed getDireccionLaboral() { return direccionLaboral; }
    public void setDireccionLaboral(DireccionEmbed v) { this.direccionLaboral = v; }
    public String getEmpresa() { return empresa; }
    public void setEmpresa(String v) { this.empresa = v; }
    public String getDepartamento() { return departamento; }
    public void setDepartamento(String v) { this.departamento = v; }
    public String getCargo() { return cargo; }
    public void setCargo(String v) { this.cargo = v; }
    public TipoContrato getTipoContrato() { return tipoContrato; }
    public void setTipoContrato(TipoContrato v) { this.tipoContrato = v; }
    public java.math.BigDecimal getSalario() { return salario; }
    public void setSalario(java.math.BigDecimal v) { this.salario = v; }
    public java.math.BigDecimal getMontoAhorro() { return montoAhorro; }
    public void setMontoAhorro(java.math.BigDecimal v) { this.montoAhorro = v; }
    public String getNumeroCuentaNomina() { return numeroCuentaNomina; }
    public void setNumeroCuentaNomina(String v) { this.numeroCuentaNomina = v; }
    public String getBancoNomina() { return bancoNomina; }
    public void setBancoNomina(String v) { this.bancoNomina = v; }
    public ContactoEmergenciaEmbed getContactoEmergencia() { return contactoEmergencia; }
    public void setContactoEmergencia(ContactoEmergenciaEmbed v) { this.contactoEmergencia = v; }
    public EstadoSocio getEstado() { return estado; }
    public void setEstado(EstadoSocio v) { this.estado = v; }
    public LocalDate getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDate v) { this.fechaIngreso = v; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime v) { this.fechaRegistro = v; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime v) { this.fechaActualizacion = v; }
    public LocalDateTime getFechaActivacion() { return fechaActivacion; }
    public void setFechaActivacion(LocalDateTime v) { this.fechaActivacion = v; }
    public LocalDateTime getFechaDesactivacion() { return fechaDesactivacion; }
    public void setFechaDesactivacion(LocalDateTime v) { this.fechaDesactivacion = v; }
    public String getMotivoDesactivacion() { return motivoDesactivacion; }
    public void setMotivoDesactivacion(String v) { this.motivoDesactivacion = v; }
    public java.util.Set<String> getRoles() { return roles; }
    public void setRoles(java.util.Set<String> v) { this.roles = v; }

    public static SocioEntityBuilder builder() { return new SocioEntityBuilder(); }

    public static class SocioEntityBuilder {
        private SocioEntity e = new SocioEntity();
        public SocioEntityBuilder id(UUID v) { e.id = v; return this; }
        public SocioEntityBuilder numeroSocio(String v) { e.numeroSocio = v; return this; }
        public SocioEntityBuilder primerNombre(String v) { e.primerNombre = v; return this; }
        public SocioEntityBuilder segundoNombre(String v) { e.segundoNombre = v; return this; }
        public SocioEntityBuilder primerApellido(String v) { e.primerApellido = v; return this; }
        public SocioEntityBuilder segundoApellido(String v) { e.segundoApellido = v; return this; }
        public SocioEntityBuilder fechaNacimiento(LocalDate v) { e.fechaNacimiento = v; return this; }
        public SocioEntityBuilder genero(Genero v) { e.genero = v; return this; }
        public SocioEntityBuilder estadoCivil(EstadoCivil v) { e.estadoCivil = v; return this; }
        public SocioEntityBuilder tipoDocumento(TipoDocumento v) { e.tipoDocumento = v; return this; }
        public SocioEntityBuilder numeroDocumento(String v) { e.numeroDocumento = v; return this; }
        public SocioEntityBuilder correoElectronico(String v) { e.correoElectronico = v; return this; }
        public SocioEntityBuilder telefonoPrincipal(String v) { e.telefonoPrincipal = v; return this; }
        public SocioEntityBuilder telefonoSecundario(String v) { e.telefonoSecundario = v; return this; }
        public SocioEntityBuilder direccionResidencia(DireccionEmbed v) { e.direccionResidencia = v; return this; }
        public SocioEntityBuilder direccionLaboral(DireccionEmbed v) { e.direccionLaboral = v; return this; }
        public SocioEntityBuilder empresa(String v) { e.empresa = v; return this; }
        public SocioEntityBuilder departamento(String v) { e.departamento = v; return this; }
        public SocioEntityBuilder cargo(String v) { e.cargo = v; return this; }
        public SocioEntityBuilder tipoContrato(TipoContrato v) { e.tipoContrato = v; return this; }
        public SocioEntityBuilder salario(java.math.BigDecimal v) { e.salario = v; return this; }
        public SocioEntityBuilder montoAhorro(java.math.BigDecimal v) { e.montoAhorro = v; return this; }
        public SocioEntityBuilder numeroCuentaNomina(String v) { e.numeroCuentaNomina = v; return this; }
        public SocioEntityBuilder bancoNomina(String v) { e.bancoNomina = v; return this; }
        public SocioEntityBuilder contactoEmergencia(ContactoEmergenciaEmbed v) { e.contactoEmergencia = v; return this; }
        public SocioEntityBuilder estado(EstadoSocio v) { e.estado = v; return this; }
        public SocioEntityBuilder fechaIngreso(LocalDate v) { e.fechaIngreso = v; return this; }
        public SocioEntityBuilder fechaRegistro(LocalDateTime v) { e.fechaRegistro = v; return this; }
        public SocioEntityBuilder fechaActualizacion(LocalDateTime v) { e.fechaActualizacion = v; return this; }
        public SocioEntityBuilder fechaActivacion(LocalDateTime v) { e.fechaActivacion = v; return this; }
        public SocioEntityBuilder fechaDesactivacion(LocalDateTime v) { e.fechaDesactivacion = v; return this; }
        public SocioEntityBuilder motivoDesactivacion(String v) { e.motivoDesactivacion = v; return this; }
        public SocioEntityBuilder roles(java.util.Set<String> v) { e.roles = v; return this; }
        public SocioEntity build() { return e; }
    }

    @Embeddable
    public static class DireccionEmbed {
        private String calle;
        private String numero;
        private String colonia;
        private String ciudad;
        private String estado;
        private String codigoPostal;
        private String pais;

        public DireccionEmbed() {}
        public String getCalle() { return calle; }
        public void setCalle(String v) { this.calle = v; }
        public String getNumero() { return numero; }
        public void setNumero(String v) { this.numero = v; }
        public String getColonia() { return colonia; }
        public void setColonia(String v) { this.colonia = v; }
        public String getCiudad() { return ciudad; }
        public void setCiudad(String v) { this.ciudad = v; }
        public String getEstado() { return estado; }
        public void setEstado(String v) { this.estado = v; }
        public String getCodigoPostal() { return codigoPostal; }
        public void setCodigoPostal(String v) { this.codigoPostal = v; }
        public String getPais() { return pais; }
        public void setPais(String v) { this.pais = v; }

        public static DireccionEmbedBuilder builder() { return new DireccionEmbedBuilder(); }
        public static class DireccionEmbedBuilder {
            private DireccionEmbed d = new DireccionEmbed();
            public DireccionEmbedBuilder calle(String v) { d.calle = v; return this; }
            public DireccionEmbedBuilder numero(String v) { d.numero = v; return this; }
            public DireccionEmbedBuilder colonia(String v) { d.colonia = v; return this; }
            public DireccionEmbedBuilder ciudad(String v) { d.ciudad = v; return this; }
            public DireccionEmbedBuilder estado(String v) { d.estado = v; return this; }
            public DireccionEmbedBuilder codigoPostal(String v) { d.codigoPostal = v; return this; }
            public DireccionEmbedBuilder pais(String v) { d.pais = v; return this; }
            public DireccionEmbed build() { return d; }
        }
    }

    @Embeddable
    public static class ContactoEmergenciaEmbed {
        private String nombre;
        private String parentesco;
        private String telefono;

        public ContactoEmergenciaEmbed() {}
        public String getNombre() { return nombre; }
        public void setNombre(String v) { this.nombre = v; }
        public String getParentesco() { return parentesco; }
        public void setParentesco(String v) { this.parentesco = v; }
        public String getTelefono() { return telefono; }
        public void setTelefono(String v) { this.telefono = v; }

        public static ContactoEmergenciaEmbedBuilder builder() { return new ContactoEmergenciaEmbedBuilder(); }
        public static class ContactoEmergenciaEmbedBuilder {
            private ContactoEmergenciaEmbed c = new ContactoEmergenciaEmbed();
            public ContactoEmergenciaEmbedBuilder nombre(String v) { c.nombre = v; return this; }
            public ContactoEmergenciaEmbedBuilder parentesco(String v) { c.parentesco = v; return this; }
            public ContactoEmergenciaEmbedBuilder telefono(String v) { c.telefono = v; return this; }
            public ContactoEmergenciaEmbed build() { return c; }
        }
    }
}
