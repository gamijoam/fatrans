// com/tufondo/creditos/application/dto/EvaluarSolicitudRequest.java
package com.tufondo.creditos.application.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para evaluar una solicitud de crédito.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluarSolicitudRequest {
    
    @NotNull(message = "puntajeAntiguedad es requerido")
    @Min(value = 0, message = "puntaje debe ser >= 0")
    @Max(value = 30, message = "puntaje máximo es 30")
    private Integer puntajeAntiguedad;
    
    @NotNull(message = "puntajeHistorialAhorro es requerido")
    @Min(value = 0, message = "puntaje debe ser >= 0")
    @Max(value = 30, message = "puntaje máximo es 30")
    private Integer puntajeHistorialAhorro;
    
    @NotNull(message = "puntajeCapacidadPago es requerido")
    @Min(value = 0, message = "puntaje debe ser >= 0")
    @Max(value = 40, message = "puntaje máximo es 40")
    private Integer puntajeCapacidadPago;
    
    @NotNull(message = "salarioEstimado es requerido")
    @DecimalMin(value = "0.01", message = "salario debe ser > 0")
    private BigDecimal salarioEstimado;
}
