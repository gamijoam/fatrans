// 📁 com/tufondo/socios/application/dto/SolicitudRegistroRequestDTO.java
package com.tufondo.socios.application.dto;

import com.tufondo.socios.application.validation.MayorDeEdad;
import com.tufondo.socios.domain.model.enums.EstadoCivil;
import com.tufondo.socios.domain.model.enums.Genero;
import com.tufondo.socios.domain.model.enums.TipoDocumento;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para solicitud de registro de nuevo socio.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudRegistroRequestDTO {

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombreCompleto;

    @NotNull(message = "El tipo de documento es obligatorio")
    private TipoDocumento tipoDocumento;

    @NotBlank(message = "La cédula es obligatoria")
    @Pattern(regexp = "^[VE]-\\d{7,8}$", message = "Formato de cédula inválido. Use V-12345678 o E-12345678")
    private String cedula;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
    @MayorDeEdad(edadMinima = 18, message = "Debes tener al menos 18 años para registrarte")
    private LocalDate fechaNacimiento;

    @NotNull(message = "El género es obligatorio")
    private Genero genero;

    @NotNull(message = "El estado civil es obligatorio")
    private EstadoCivil estadoCivil;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El formato del correo electrónico es inválido")
    private String correoElectronico;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "El teléfono debe tener 10 o 11 dígitos")
    private String telefono;

    @NotBlank(message = "La empresa es obligatoria")
    @Size(min = 2, max = 200, message = "El nombre de la empresa debe tener entre 2 y 200 caracteres")
    private String empresa;

    private String rifEmpresa;

    @Size(max = 100, message = "El departamento debe tener máximo 100 caracteres")
    private String departamento;

    @Size(max = 100, message = "El cargo debe tener máximo 100 caracteres")
    private String cargo;

    @DecimalMin(value = "0.01", message = "El salario debe ser mayor a cero")
    @DecimalMax(value = "999999999.99", message = "El salario excede el límite permitido")
    private BigDecimal salario;

    @Size(max = 100, message = "El estado debe tener máximo 100 caracteres")
    private String direccionEstado;

    @Size(max = 100, message = "La ciudad debe tener máximo 100 caracteres")
    private String direccionCiudad;

    @Size(max = 100, message = "El municipio debe tener máximo 100 caracteres")
    private String direccionMunicipio;

    @Size(max = 255, message = "La calle debe tener máximo 255 caracteres")
    private String direccionCalle;

    @Size(max = 200, message = "El nombre de emergencia debe tener máximo 200 caracteres")
    private String emergenciaNombre;

    @Pattern(regexp = "^[0-9]{10,11}$", message = "El teléfono de emergencia debe tener 10 o 11 dígitos")
    private String emergenciaTelefono;

    @Size(max = 50, message = "El parentesco debe tener máximo 50 caracteres")
    private String emergenciaParentesco;

    @NotNull(message = "Debe aceptar los términos y condiciones")
    @AssertTrue(message = "Debe aceptar los términos y condiciones")
    private Boolean aceptaTerminos;

    @NotNull(message = "Debe aceptar la política de protección de datos")
    @AssertTrue(message = "Debe aceptar la política de protección de datos personales")
    private Boolean aceptaLopdp;

    /**
     * Declaración jurada LOCDOFT (origen lícito de fondos). Issue #218 PR-B.
     * Obligación de la Ley Orgánica contra la Delincuencia Organizada y
     * Financiamiento al Terrorismo — los sujetos obligados (Fatrans como
     * fondo de ahorro) deben recolectar esta declaración al onboarding.
     */
    @NotNull(message = "Debe aceptar la declaración LOCDOFT")
    @AssertTrue(message = "Debe declarar que los fondos provienen de actividades lícitas (LOCDOFT)")
    private Boolean aceptaLocdoft;

    // ---- Metadatos opcionales de auditoría LOPDP ----
    // Provienen del BFF (que ya captura x-forwarded-for + user-agent).
    // El controller usa estos valores como fuente preferida y, si vienen vacíos,
    // hace fallback a HttpServletRequest. El consentLopdpTimestamp NO viaja
    // desde el cliente: lo sella el backend (Instant.now()) cuando aceptaLopdp == true.
    @Size(max = 45, message = "La IP de registro debe tener máximo 45 caracteres")
    private String ipRegistro;

    @Size(max = 500, message = "El User-Agent debe tener máximo 500 caracteres")
    private String userAgentRegistro;
}