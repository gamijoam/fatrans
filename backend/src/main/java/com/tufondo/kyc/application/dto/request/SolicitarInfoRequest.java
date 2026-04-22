// com.tufondo.kyc.application.dto.request.SolicitarInfoRequest
package com.tufondo.kyc.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitar informacion adicional.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitarInfoRequest {

    @NotBlank(message = "comentario es requerido")
    @Size(min = 10, max = 1000, message = "comentario debe tener entre 10 y 1000 caracteres")
    private String comentario;
}