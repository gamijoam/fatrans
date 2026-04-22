// com/tufondo/socios/application/dto/SocioResponseDTO.java
package com.tufondo.socios.application.dto;

import com.tufondo.socios.domain.model.enums.*;
import com.tufondo.socios.domain.model.valueobjects.Direccion;
import com.tufondo.socios.domain.model.valueobjects.ContactoEmergencia;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class SocioResponseDTO {
    private java.util.UUID id;
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
    private java.time.LocalDateTime fechaRegistro;
    private java.time.LocalDateTime fechaActualizacion;
    private java.time.LocalDateTime fechaActivacion;
    private java.time.LocalDateTime fechaDesactivacion;
    private String motivoDesactivacion;
    private java.util.Set<String> roles;

    public SocioResponseDTO() {}

    // Getters and Setters
    public java.util.UUID getId() { return id; }
    public void setId(java.util.UUID v) { this.id = v; }
    public String getNumeroSocio() { return numeroSocio; }
    public void setNumeroSocio(String v) { this.numeroSocio = v; }
    public TipoDocumento getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(TipoDocumento v) { this.tipoDocumento = v; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String v) { this.numeroDocumento = v; }
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
    public String getCorreoElectronico() { return correoElectronico; }
    public void setCorreoElectronico(String v) { this.correoElectronico = v; }
    public String getTelefonoPrincipal() { return telefonoPrincipal; }
    public void setTelefonoPrincipal(String v) { this.telefonoPrincipal = v; }
    public String getTelefonoSecundario() { return telefonoSecundario; }
    public void setTelefonoSecundario(String v) { this.telefonoSecundario = v; }
    public Direccion getDireccionResidencia() { return direccionResidencia; }
    public void setDireccionResidencia(Direccion v) { this.direccionResidencia = v; }
    public Direccion getDireccionLaboral() { return direccionLaboral; }
    public void setDireccionLaboral(Direccion v) { this.direccionLaboral = v; }
    public String getEmpresa() { return empresa; }
    public void setEmpresa(String v) { this.empresa = v; }
    public String getDepartamento() { return departamento; }
    public void setDepartamento(String v) { this.departamento = v; }
    public String getCargo() { return cargo; }
    public void setCargo(String v) { this.cargo = v; }
    public TipoContrato getTipoContrato() { return tipoContrato; }
    public void setTipoContrato(TipoContrato v) { this.tipoContrato = v; }
    // SECURITY: Getters enmascarados para datos financieros sensibles
    public BigDecimal getSalario() { 
        return salario != null ? salario : BigDecimal.ZERO; 
    }
    public BigDecimal getMontoAhorro() {
        return montoAhorro != null ? montoAhorro : BigDecimal.ZERO;
    }
    public String getNumeroCuentaNomina() {
        if (numeroCuentaNomina == null || numeroCuentaNomina.length() < 4) {
            return null;
        }
        return "****" + numeroCuentaNomina.substring(numeroCuentaNomina.length() - 4);
    }
    public String getBancoNomina() {
        return bancoNomina; // Solo mostrar si no es null
    }
    public ContactoEmergencia getContactoEmergencia() { return contactoEmergencia; }
    public void setContactoEmergencia(ContactoEmergencia v) { this.contactoEmergencia = v; }
    public EstadoSocio getEstado() { return estado; }
    public void setEstado(EstadoSocio v) { this.estado = v; }
    public LocalDate getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDate v) { this.fechaIngreso = v; }
    public java.time.LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(java.time.LocalDateTime v) { this.fechaRegistro = v; }
    public java.time.LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(java.time.LocalDateTime v) { this.fechaActualizacion = v; }
    public java.time.LocalDateTime getFechaActivacion() { return fechaActivacion; }
    public void setFechaActivacion(java.time.LocalDateTime v) { this.fechaActivacion = v; }
    public java.time.LocalDateTime getFechaDesactivacion() { return fechaDesactivacion; }
    public void setFechaDesactivacion(java.time.LocalDateTime v) { this.fechaDesactivacion = v; }
    public String getMotivoDesactivacion() { return motivoDesactivacion; }
    public void setMotivoDesactivacion(String v) { this.motivoDesactivacion = v; }
    public java.util.Set<String> getRoles() { return roles; }
    public void setRoles(java.util.Set<String> v) { this.roles = v; }

    public static SocioResponseDTOBuilder builder() { return new SocioResponseDTOBuilder(); }

    public static class SocioResponseDTOBuilder {
        private SocioResponseDTO d = new SocioResponseDTO();
        public SocioResponseDTOBuilder id(java.util.UUID v) { d.id = v; return this; }
        public SocioResponseDTOBuilder numeroSocio(String v) { d.numeroSocio = v; return this; }
        public SocioResponseDTOBuilder tipoDocumento(TipoDocumento v) { d.tipoDocumento = v; return this; }
        public SocioResponseDTOBuilder numeroDocumento(String v) { d.numeroDocumento = v; return this; }
        public SocioResponseDTOBuilder primerNombre(String v) { d.primerNombre = v; return this; }
        public SocioResponseDTOBuilder segundoNombre(String v) { d.segundoNombre = v; return this; }
        public SocioResponseDTOBuilder primerApellido(String v) { d.primerApellido = v; return this; }
        public SocioResponseDTOBuilder segundoApellido(String v) { d.segundoApellido = v; return this; }
        public SocioResponseDTOBuilder fechaNacimiento(LocalDate v) { d.fechaNacimiento = v; return this; }
        public SocioResponseDTOBuilder genero(Genero v) { d.genero = v; return this; }
        public SocioResponseDTOBuilder estadoCivil(EstadoCivil v) { d.estadoCivil = v; return this; }
        public SocioResponseDTOBuilder correoElectronico(String v) { d.correoElectronico = v; return this; }
        public SocioResponseDTOBuilder telefonoPrincipal(String v) { d.telefonoPrincipal = v; return this; }
        public SocioResponseDTOBuilder telefonoSecundario(String v) { d.telefonoSecundario = v; return this; }
        public SocioResponseDTOBuilder direccionResidencia(Direccion v) { d.direccionResidencia = v; return this; }
        public SocioResponseDTOBuilder direccionLaboral(Direccion v) { d.direccionLaboral = v; return this; }
        public SocioResponseDTOBuilder empresa(String v) { d.empresa = v; return this; }
        public SocioResponseDTOBuilder departamento(String v) { d.departamento = v; return this; }
        public SocioResponseDTOBuilder cargo(String v) { d.cargo = v; return this; }
        public SocioResponseDTOBuilder tipoContrato(TipoContrato v) { d.tipoContrato = v; return this; }
        public SocioResponseDTOBuilder salario(BigDecimal v) { d.salario = v; return this; }
        public SocioResponseDTOBuilder montoAhorro(BigDecimal v) { d.montoAhorro = v; return this; }
        public SocioResponseDTOBuilder numeroCuentaNomina(String v) { d.numeroCuentaNomina = v; return this; }
        public SocioResponseDTOBuilder bancoNomina(String v) { d.bancoNomina = v; return this; }
        public SocioResponseDTOBuilder contactoEmergencia(ContactoEmergencia v) { d.contactoEmergencia = v; return this; }
        public SocioResponseDTOBuilder estado(EstadoSocio v) { d.estado = v; return this; }
        public SocioResponseDTOBuilder fechaIngreso(LocalDate v) { d.fechaIngreso = v; return this; }
        public SocioResponseDTOBuilder fechaRegistro(java.time.LocalDateTime v) { d.fechaRegistro = v; return this; }
        public SocioResponseDTOBuilder fechaActualizacion(java.time.LocalDateTime v) { d.fechaActualizacion = v; return this; }
        public SocioResponseDTOBuilder fechaActivacion(java.time.LocalDateTime v) { d.fechaActivacion = v; return this; }
        public SocioResponseDTOBuilder fechaDesactivacion(java.time.LocalDateTime v) { d.fechaDesactivacion = v; return this; }
        public SocioResponseDTOBuilder motivoDesactivacion(String v) { d.motivoDesactivacion = v; return this; }
        public SocioResponseDTOBuilder roles(java.util.Set<String> v) { d.roles = v; return this; }
        public SocioResponseDTO build() { return d; }
    }
}
