// com.tufondo.kyc.application.dto.request.RechazarVerificacionRequest
package com.tufondo.kyc.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO para rechazar una verificacion.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RechazarVerificacionRequest {

    @NotBlank(message = "comentario es requerido")
    @Size(min = 10, max = 1000, message = "comentario debe tener entre 10 y 1000 caracteres")
    private String comentario;

    private List<UUID> documentosRechazados;
}