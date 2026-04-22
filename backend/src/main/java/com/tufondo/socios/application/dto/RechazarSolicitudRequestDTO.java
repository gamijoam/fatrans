// 📁 com.tufondo.socios.application.dto.RechazarSolicitudRequestDTO.java
package com.tufondo.socios.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para rechazar una solicitud de registro.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RechazarSolicitudRequestDTO {
    
    @NotBlank(message = "El motivo de rechazo es obligatorio")
    private String motivo;
}