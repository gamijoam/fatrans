// 📁 com/tufondo/socios/application/dto/SolicitudRegistroResponseDTO.java
package com.tufondo.socios.application.dto;

import com.tufondo.socios.domain.model.enums.EstadoSolicitud;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para solicitud de registro.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudRegistroResponseDTO {
    
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
}