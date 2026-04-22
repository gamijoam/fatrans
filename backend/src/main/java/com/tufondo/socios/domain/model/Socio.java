// com/tufondo/socios/domain/model/Socio.java
package com.tufondo.socios.domain.model;

import com.tufondo.socios.domain.model.enums.*;
import com.tufondo.socios.domain.model.valueobjects.Direccion;
import com.tufondo.socios.domain.model.valueobjects.ContactoEmergencia;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public class Socio {
    private UUID id;
    private String numeroSocio;
    private TipoDocumento tipoDocumento;
    private String numeroDocumento;
    private String primerNombre;
    private String segundoNombre;
    private String primerApellido;
    private String segundoApellido;
    private LocalDate fechaNacimiento;
    private Genero genero;
    private EstadoCivil estadoCivil;
    private String correoElectronico;
    private String telefonoPrincipal;
    private String telefonoSecundario;
    private Direccion direccionResidencia;
    private Direccion direccionLaboral;
    private String empresa;
    private String departamento;
    private String cargo;
    private TipoContrato tipoContrato;
    private BigDecimal salario;
    private BigDecimal montoAhorro;
    private String numeroCuentaNomina;
    private String bancoNomina;
    private ContactoEmergencia contactoEmergencia;
    private EstadoSocio estado;
    private LocalDate fechaIngreso;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
    private LocalDateTime fechaActivacion;
    private LocalDateTime fechaDesactivacion;
    private String motivoDesactivacion;
    private Set<String> roles;

    public Socio() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getNumeroSocio() { return numeroSocio; }
    public void setNumeroSocio(String numeroSocio) { this.numeroSocio = numeroSocio; }
    public TipoDocumento getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(TipoDocumento tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }
    public String getPrimerNombre() { return primerNombre; }
    public void setPrimerNombre(String primerNombre) { this.primerNombre = primerNombre; }
    public String getSegundoNombre() { return segundoNombre; }
    public void setSegundoNombre(String segundoNombre) { this.segundoNombre = segundoNombre; }
    public String getPrimerApellido() { return primerApellido; }
    public void setPrimerApellido(String primerApellido) { this.primerApellido = primerApellido; }
    public String getSegundoApellido() { return segundoApellido; }
    public void setSegundoApellido(String segundoApellido) { this.segundoApellido = segundoApellido; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public Genero getGenero() { return genero; }
    public void setGenero(Genero genero) { this.genero = genero; }
    public EstadoCivil getEstadoCivil() { return estadoCivil; }
    public void setEstadoCivil(EstadoCivil estadoCivil) { this.estadoCivil = estadoCivil; }
    public String getCorreoElectronico() { return correoElectronico; }
    public void setCorreoElectronico(String correoElectronico) { this.correoElectronico = correoElectronico; }
    public String getTelefonoPrincipal() { return telefonoPrincipal; }
    public void setTelefonoPrincipal(String telefonoPrincipal) { this.telefonoPrincipal = telefonoPrincipal; }
    public String getTelefonoSecundario() { return telefonoSecundario; }
    public void setTelefonoSecundario(String telefonoSecundario) { this.telefonoSecundario = telefonoSecundario; }
    public Direccion getDireccionResidencia() { return direccionResidencia; }
    public void setDireccionResidencia(Direccion direccionResidencia) { this.direccionResidencia = direccionResidencia; }
    public Direccion getDireccionLaboral() { return direccionLaboral; }
    public void setDireccionLaboral(Direccion direccionLaboral) { this.direccionLaboral = direccionLaboral; }
    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }
    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }
    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    public TipoContrato getTipoContrato() { return tipoContrato; }
    public void setTipoContrato(TipoContrato tipoContrato) { this.tipoContrato = tipoContrato; }
    public BigDecimal getSalario() { return salario; }
    public void setSalario(BigDecimal salario) { this.salario = salario; }
    public BigDecimal getMontoAhorro() { return montoAhorro; }
    public void setMontoAhorro(BigDecimal montoAhorro) { this.montoAhorro = montoAhorro; }
    public String getNumeroCuentaNomina() { return numeroCuentaNomina; }
    public void setNumeroCuentaNomina(String numeroCuentaNomina) { this.numeroCuentaNomina = numeroCuentaNomina; }
    public String getBancoNomina() { return bancoNomina; }
    public void setBancoNomina(String bancoNomina) { this.bancoNomina = bancoNomina; }
    public ContactoEmergencia getContactoEmergencia() { return contactoEmergencia; }
    public void setContactoEmergencia(ContactoEmergencia contactoEmergencia) { this.contactoEmergencia = contactoEmergencia; }
    public EstadoSocio getEstado() { return estado; }
    public void setEstado(EstadoSocio estado) { this.estado = estado; }
    public LocalDate getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDate fechaIngreso) { this.fechaIngreso = fechaIngreso; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    public LocalDateTime getFechaActivacion() { return fechaActivacion; }
    public void setFechaActivacion(LocalDateTime fechaActivacion) { this.fechaActivacion = fechaActivacion; }
    public LocalDateTime getFechaDesactivacion() { return fechaDesactivacion; }
    public void setFechaDesactivacion(LocalDateTime fechaDesactivacion) { this.fechaDesactivacion = fechaDesactivacion; }
    public String getMotivoDesactivacion() { return motivoDesactivacion; }
    public void setMotivoDesactivacion(String motivoDesactivacion) { this.motivoDesactivacion = motivoDesactivacion; }
    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }

    public static SocioBuilder builder() { return new SocioBuilder(); }

    public static class SocioBuilder {
        private Socio s = new Socio();
        public SocioBuilder id(UUID v) { s.id = v; return this; }
        public SocioBuilder numeroSocio(String v) { s.numeroSocio = v; return this; }
        public SocioBuilder tipoDocumento(TipoDocumento v) { s.tipoDocumento = v; return this; }
        public SocioBuilder numeroDocumento(String v) { s.numeroDocumento = v; return this; }
        public SocioBuilder primerNombre(String v) { s.primerNombre = v; return this; }
        public SocioBuilder segundoNombre(String v) { s.segundoNombre = v; return this; }
        public SocioBuilder primerApellido(String v) { s.primerApellido = v; return this; }
        public SocioBuilder segundoApellido(String v) { s.segundoApellido = v; return this; }
        public SocioBuilder fechaNacimiento(LocalDate v) { s.fechaNacimiento = v; return this; }
        public SocioBuilder genero(Genero v) { s.genero = v; return this; }
        public SocioBuilder estadoCivil(EstadoCivil v) { s.estadoCivil = v; return this; }
        public SocioBuilder correoElectronico(String v) { s.correoElectronico = v; return this; }
        public SocioBuilder telefonoPrincipal(String v) { s.telefonoPrincipal = v; return this; }
        public SocioBuilder telefonoSecundario(String v) { s.telefonoSecundario = v; return this; }
        public SocioBuilder direccionResidencia(Direccion v) { s.direccionResidencia = v; return this; }
        public SocioBuilder direccionLaboral(Direccion v) { s.direccionLaboral = v; return this; }
        public SocioBuilder empresa(String v) { s.empresa = v; return this; }
        public SocioBuilder departamento(String v) { s.departamento = v; return this; }
        public SocioBuilder cargo(String v) { s.cargo = v; return this; }
        public SocioBuilder tipoContrato(TipoContrato v) { s.tipoContrato = v; return this; }
        public SocioBuilder salario(BigDecimal v) { s.salario = v; return this; }
        public SocioBuilder montoAhorro(BigDecimal v) { s.montoAhorro = v; return this; }
        public SocioBuilder numeroCuentaNomina(String v) { s.numeroCuentaNomina = v; return this; }
        public SocioBuilder bancoNomina(String v) { s.bancoNomina = v; return this; }
        public SocioBuilder contactoEmergencia(ContactoEmergencia v) { s.contactoEmergencia = v; return this; }
        public SocioBuilder estado(EstadoSocio v) { s.estado = v; return this; }
        public SocioBuilder fechaIngreso(LocalDate v) { s.fechaIngreso = v; return this; }
        public SocioBuilder fechaRegistro(LocalDateTime v) { s.fechaRegistro = v; return this; }
        public SocioBuilder fechaActualizacion(LocalDateTime v) { s.fechaActualizacion = v; return this; }
        public SocioBuilder fechaActivacion(LocalDateTime v) { s.fechaActivacion = v; return this; }
        public SocioBuilder fechaDesactivacion(LocalDateTime v) { s.fechaDesactivacion = v; return this; }
        public SocioBuilder motivoDesactivacion(String v) { s.motivoDesactivacion = v; return this; }
        public SocioBuilder roles(Set<String> v) { s.roles = v; return this; }
        public Socio build() { return s; }
    }

    public void activar() {
        this.estado = EstadoSocio.ACTIVO;
        this.fechaActivacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
    }

    public void desactivar(String motivo) {
        this.estado = EstadoSocio.INACTIVO;
        this.fechaDesactivacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        this.motivoDesactivacion = motivo;
    }

    public void eliminar(String motivo) {
        this.estado = EstadoSocio.ELIMINADO;
        this.fechaDesactivacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        this.motivoDesactivacion = "BORRADO: " + motivo;
    }
}
