// 📁 com/tufondo/socios/application/dto/SolicitudRegistroResponseDTO.java
package com.tufondo.socios.application.dto;

import com.tufondo.socios.domain.model.enums.EstadoCivil;
import com.tufondo.socios.domain.model.enums.EstadoSolicitud;
import com.tufondo.socios.domain.model.enums.Genero;
import com.tufondo.socios.domain.model.enums.TipoDocumento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para solicitud de registro.
 *
 * Antes solo exponía 12 campos básicos (nombre, cédula, contacto, empresa) y el
 * admin no podía ver los datos completos al revisar una solicitud — quedaba a
 * ciegas sobre dirección, datos laborales, contacto de emergencia, consentimientos
 * legales, etc. Ahora incluye todos los datos relevantes para que el panel de admin
 * muestre el detalle completo y pueda decidir aprobar o rechazar con contexto.
 *
 * No incluimos la metadata de auditoría LOPDP (ip, user-agent, consent timestamp).
 * Eso solo se expone vía un endpoint específico de auditoría — el admin operativo
 * no necesita verlo en el flujo normal.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudRegistroResponseDTO {

    private UUID id;

    // --- Datos personales ---
    private String nombreCompleto;
    private TipoDocumento tipoDocumento;
    private String cedula;
    private LocalDate fechaNacimiento;
    private Genero genero;
    private EstadoCivil estadoCivil;

    // --- Contacto ---
    private String correoElectronico;
    private String telefono;

    // --- Información laboral ---
    private String empresa;
    private String rifEmpresa;
    private String departamento;
    private String cargo;
    private BigDecimal salario;

    // --- Dirección de residencia ---
    private String direccionEstado;
    private String direccionCiudad;
    private String direccionMunicipio;
    private String direccionCalle;

    // --- Contacto de emergencia ---
    private String emergenciaNombre;
    private String emergenciaTelefono;
    private String emergenciaParentesco;

    // --- Consentimientos legales ---
    private Boolean aceptaTerminos;
    private Boolean aceptaLopdp;
    /** Declaración LOCDOFT (#218 PR-B) — origen lícito de fondos. */
    private Boolean aceptaLocdoft;

    // --- Estado y trazabilidad de revisión ---
    private EstadoSolicitud estado;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaRevision;
    private String revisadoPor;
    private String comentario;
    private String motivoRechazo;
}
