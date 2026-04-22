// 📁 com/tufondo/socios/application/dto/ActualizarSocioDTO.java
// 🔧 SECURITY FIX: Eliminados campos 'estado' y 'roles' para prevenir Mass Assignment
// La gestión de estado se realiza exclusivamente via use cases dedicados (Activar/Desactivar/Eliminar)
package com.tufondo.socios.application.dto;

import com.tufondo.socios.domain.model.enums.*;
import com.tufondo.socios.domain.model.valueobjects.ContactoEmergencia;
import com.tufondo.socios.domain.model.valueobjects.Direccion;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

public class ActualizarSocioDTO {
    private String primerNombre;
    private String segundoNombre;
    private String primerApellido;
    private String segundoApellido;
    private TipoDocumento tipoDocumento;
    private String numeroDocumento;
    private Genero genero;
    @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
    private LocalDate fechaNacimiento;
    private EstadoCivil estadoCivil;
    @Email(message = "Formato de correo electrónico inválido")
    private String correoElectronico;
    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Formato de teléfono principal inválido")
    private String telefonoPrincipal;
    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Formato de teléfono secundario inválido")
    private String telefonoSecundario;
    private Direccion direccionResidencia;
    private Direccion direccionLaboral;
    private String empresa;
    private String departamento;
    private String cargo;
    private TipoContrato tipoContrato;
    @Pattern(regexp = "^[0-9]{10,20}$", message = "Formato de número de cuenta nómina inválido")
    private String numeroCuentaNomina;
    private String bancoNomina;
    private ContactoEmergencia contactoEmergencia;

    public ActualizarSocioDTO() {}

    // Getters and Setters
    public String getPrimerNombre() { return primerNombre; }
    public void setPrimerNombre(String v) { this.primerNombre = v; }
    public String getSegundoNombre() { return segundoNombre; }
    public void setSegundoNombre(String v) { this.segundoNombre = v; }
    public String getPrimerApellido() { return primerApellido; }
    public void setPrimerApellido(String v) { this.primerApellido = v; }
    public String getSegundoApellido() { return segundoApellido; }
    public void setSegundoApellido(String v) { this.segundoApellido = v; }
    public TipoDocumento getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(TipoDocumento v) { this.tipoDocumento = v; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String v) { this.numeroDocumento = v; }
    public Genero getGenero() { return genero; }
    public void setGenero(Genero v) { this.genero = v; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate v) { this.fechaNacimiento = v; }
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
    public String getNumeroCuentaNomina() { return numeroCuentaNomina; }
    public void setNumeroCuentaNomina(String v) { this.numeroCuentaNomina = v; }
    public String getBancoNomina() { return bancoNomina; }
    public void setBancoNomina(String v) { this.bancoNomina = v; }
    public ContactoEmergencia getContactoEmergencia() { return contactoEmergencia; }
    public void setContactoEmergencia(ContactoEmergencia v) { this.contactoEmergencia = v; }

    public static ActualizarSocioDTOBuilder builder() { return new ActualizarSocioDTOBuilder(); }

    public static class ActualizarSocioDTOBuilder {
        private ActualizarSocioDTO d = new ActualizarSocioDTO();
        public ActualizarSocioDTOBuilder primerNombre(String v) { d.primerNombre = v; return this; }
        public ActualizarSocioDTOBuilder segundoNombre(String v) { d.segundoNombre = v; return this; }
        public ActualizarSocioDTOBuilder primerApellido(String v) { d.primerApellido = v; return this; }
        public ActualizarSocioDTOBuilder segundoApellido(String v) { d.segundoApellido = v; return this; }
        public ActualizarSocioDTOBuilder tipoDocumento(TipoDocumento v) { d.tipoDocumento = v; return this; }
        public ActualizarSocioDTOBuilder numeroDocumento(String v) { d.numeroDocumento = v; return this; }
        public ActualizarSocioDTOBuilder genero(Genero v) { d.genero = v; return this; }
        public ActualizarSocioDTOBuilder fechaNacimiento(LocalDate v) { d.fechaNacimiento = v; return this; }
        public ActualizarSocioDTOBuilder estadoCivil(EstadoCivil v) { d.estadoCivil = v; return this; }
        public ActualizarSocioDTOBuilder correoElectronico(String v) { d.correoElectronico = v; return this; }
        public ActualizarSocioDTOBuilder telefonoPrincipal(String v) { d.telefonoPrincipal = v; return this; }
        public ActualizarSocioDTOBuilder telefonoSecundario(String v) { d.telefonoSecundario = v; return this; }
        public ActualizarSocioDTOBuilder direccionResidencia(Direccion v) { d.direccionResidencia = v; return this; }
        public ActualizarSocioDTOBuilder direccionLaboral(Direccion v) { d.direccionLaboral = v; return this; }
        public ActualizarSocioDTOBuilder empresa(String v) { d.empresa = v; return this; }
        public ActualizarSocioDTOBuilder departamento(String v) { d.departamento = v; return this; }
        public ActualizarSocioDTOBuilder cargo(String v) { d.cargo = v; return this; }
        public ActualizarSocioDTOBuilder tipoContrato(TipoContrato v) { d.tipoContrato = v; return this; }
        public ActualizarSocioDTOBuilder numeroCuentaNomina(String v) { d.numeroCuentaNomina = v; return this; }
        public ActualizarSocioDTOBuilder bancoNomina(String v) { d.bancoNomina = v; return this; }
        public ActualizarSocioDTOBuilder contactoEmergencia(ContactoEmergencia v) { d.contactoEmergencia = v; return this; }
        public ActualizarSocioDTO build() { return d; }
    }
}
