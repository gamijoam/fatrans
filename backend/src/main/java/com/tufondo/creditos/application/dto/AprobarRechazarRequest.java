// com/tufondo/creditos/application/dto/AprobarRechazarRequest.java
package com.tufondo.creditos.application.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para aprobar o rechazar una solicitud de crédito.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AprobarRechazarRequest {

    @Size(max = 1000, message = "Comentario no puede exceder 1000 caracteres")
    private String comentario;

    @DecimalMin(value = "0.001", message = "Tasa de interés debe ser al menos 0.1%")
    @DecimalMax(value = "1.0", message = "Tasa de interés no puede exceder 100%")
    private BigDecimal tasaInteresOverride;

    @Size(max = 500, message = "Motivo de rechazo no puede exceder 500 caracteres")
    private String motivoRechazo;
}
