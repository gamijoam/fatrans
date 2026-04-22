// com/tufondo/socios/application/dto/CrearSocioRequestDTO.java
package com.tufondo.socios.application.dto;

import com.tufondo.socios.domain.model.enums.EstadoCivil;
import com.tufondo.socios.domain.model.enums.Genero;
import com.tufondo.socios.domain.model.enums.TipoContrato;
import com.tufondo.socios.domain.model.enums.TipoDocumento;
import com.tufondo.socios.domain.model.valueobjects.Direccion;
import com.tufondo.socios.domain.model.valueobjects.ContactoEmergencia;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

public class CrearSocioRequestDTO {

    @NotBlank(message = "El primer nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El primer nombre debe tener entre 2 y 100 caracteres")
    private String primerNombre;

    @Size(max = 100, message = "El segundo nombre debe tener máximo 100 caracteres")
    private String segundoNombre;

    @NotBlank(message = "El primer apellido es obligatorio")
    @Size(min = 2, max = 100, message = "El primer apellido debe tener entre 2 y 100 caracteres")
    private String primerApellido;

    @Size(max = 100, message = "El segundo apellido debe tener máximo 100 caracteres")
    private String segundoApellido;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
    private LocalDate fechaNacimiento;

    @NotNull(message = "El género es obligatorio")
    private Genero genero;

    @NotNull(message = "El estado civil es obligatorio")
    private EstadoCivil estadoCivil;

    @NotNull(message = "El tipo de documento es obligatorio")
    private TipoDocumento tipoDocumento;

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(min = 5, max = 20, message = "El número de documento debe tener entre 5 y 20 caracteres")
    private String numeroDocumento;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El formato del correo electrónico es inválido")
    private String correoElectronico;

    @Pattern(regexp = "^[0-9]{7,15}$", message = "El teléfono principal debe tener entre 7 y 15 dígitos")
    private String telefonoPrincipal;

    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "El teléfono secundario debe tener entre 7 y 15 dígitos")
    private String telefonoSecundario;

    @NotNull(message = "El tipo de contrato es obligatorio")
    private TipoContrato tipoContrato;

    @NotNull(message = "La fecha de ingreso es obligatoria")
    private LocalDate fechaIngreso;

    private String empresa;
    private String departamento;
    private String cargo;
    private java.math.BigDecimal salario;

    private Direccion direccionResidencia;
    private Direccion direccionLaboral;
    private ContactoEmergencia contactoEmergencia;

    private String numeroCuentaNomina;
    private String bancoNomina;

    public CrearSocioRequestDTO() {}

    // Getters and Setters
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
    public TipoContrato getTipoContrato() { return tipoContrato; }
    public void setTipoContrato(TipoContrato v) { this.tipoContrato = v; }
    public LocalDate getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDate v) { this.fechaIngreso = v; }
    public String getEmpresa() { return empresa; }
    public void setEmpresa(String v) { this.empresa = v; }
    public String getDepartamento() { return departamento; }
    public void setDepartamento(String v) { this.departamento = v; }
    public String getCargo() { return cargo; }
    public void setCargo(String v) { this.cargo = v; }
    public java.math.BigDecimal getSalario() { return salario; }
    public void setSalario(java.math.BigDecimal v) { this.salario = v; }
    public Direccion getDireccionResidencia() { return direccionResidencia; }
    public void setDireccionResidencia(Direccion v) { this.direccionResidencia = v; }
    public Direccion getDireccionLaboral() { return direccionLaboral; }
    public void setDireccionLaboral(Direccion v) { this.direccionLaboral = v; }
    public ContactoEmergencia getContactoEmergencia() { return contactoEmergencia; }
    public void setContactoEmergencia(ContactoEmergencia v) { this.contactoEmergencia = v; }
    public String getNumeroCuentaNomina() { return numeroCuentaNomina; }
    public void setNumeroCuentaNomina(String v) { this.numeroCuentaNomina = v; }
    public String getBancoNomina() { return bancoNomina; }
    public void setBancoNomina(String v) { this.bancoNomina = v; }

    public static CrearSocioRequestDTOBuilder builder() { return new CrearSocioRequestDTOBuilder(); }

    public static class CrearSocioRequestDTOBuilder {
        private CrearSocioRequestDTO d = new CrearSocioRequestDTO();
        public CrearSocioRequestDTOBuilder primerNombre(String v) { d.primerNombre = v; return this; }
        public CrearSocioRequestDTOBuilder segundoNombre(String v) { d.segundoNombre = v; return this; }
        public CrearSocioRequestDTOBuilder primerApellido(String v) { d.primerApellido = v; return this; }
        public CrearSocioRequestDTOBuilder segundoApellido(String v) { d.segundoApellido = v; return this; }
        public CrearSocioRequestDTOBuilder fechaNacimiento(LocalDate v) { d.fechaNacimiento = v; return this; }
        public CrearSocioRequestDTOBuilder genero(Genero v) { d.genero = v; return this; }
        public CrearSocioRequestDTOBuilder estadoCivil(EstadoCivil v) { d.estadoCivil = v; return this; }
        public CrearSocioRequestDTOBuilder tipoDocumento(TipoDocumento v) { d.tipoDocumento = v; return this; }
        public CrearSocioRequestDTOBuilder numeroDocumento(String v) { d.numeroDocumento = v; return this; }
        public CrearSocioRequestDTOBuilder correoElectronico(String v) { d.correoElectronico = v; return this; }
        public CrearSocioRequestDTOBuilder telefonoPrincipal(String v) { d.telefonoPrincipal = v; return this; }
        public CrearSocioRequestDTOBuilder telefonoSecundario(String v) { d.telefonoSecundario = v; return this; }
        public CrearSocioRequestDTOBuilder tipoContrato(TipoContrato v) { d.tipoContrato = v; return this; }
        public CrearSocioRequestDTOBuilder fechaIngreso(LocalDate v) { d.fechaIngreso = v; return this; }
        public CrearSocioRequestDTOBuilder empresa(String v) { d.empresa = v; return this; }
        public CrearSocioRequestDTOBuilder departamento(String v) { d.departamento = v; return this; }
        public CrearSocioRequestDTOBuilder cargo(String v) { d.cargo = v; return this; }
        public CrearSocioRequestDTOBuilder salario(java.math.BigDecimal v) { d.salario = v; return this; }
        public CrearSocioRequestDTOBuilder direccionResidencia(Direccion v) { d.direccionResidencia = v; return this; }
        public CrearSocioRequestDTOBuilder direccionLaboral(Direccion v) { d.direccionLaboral = v; return this; }
        public CrearSocioRequestDTOBuilder contactoEmergencia(ContactoEmergencia v) { d.contactoEmergencia = v; return this; }
        public CrearSocioRequestDTOBuilder numeroCuentaNomina(String v) { d.numeroCuentaNomina = v; return this; }
        public CrearSocioRequestDTOBuilder bancoNomina(String v) { d.bancoNomina = v; return this; }
        public CrearSocioRequestDTO build() { return d; }
    }
}
