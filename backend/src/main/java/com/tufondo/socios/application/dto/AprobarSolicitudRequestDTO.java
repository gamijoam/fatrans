// 📁 com.tufondo.socios.application.dto.AprobarSolicitudRequestDTO.java
package com.tufondo.socios.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para aprobar una solicitud de registro.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AprobarSolicitudRequestDTO {
    
    private String comentario;
}