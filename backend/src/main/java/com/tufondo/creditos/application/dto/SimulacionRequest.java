// com/tufondo/creditos/application/dto/SimulacionRequest.java
package com.tufondo.creditos.application.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para simular un crédito.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulacionRequest {
    
    @NotNull(message = "monto es requerido")
    @DecimalMin(value = "0.0001", message = "monto debe ser >= 0.0001")
    @DecimalMax(value = "5000000", message = "monto excede límite de simulación")
    private BigDecimal monto;
    
    @NotNull(message = "plazoMeses es requerido")
    @Min(value = 1, message = "plazo mínimo 1 mes")
    @Max(value = 360, message = "plazo máximo 360 meses")
    private Integer plazoMeses;
    
    @NotNull(message = "tasa es requerida")
    @DecimalMin(value = "0.0001", message = "tasa debe ser > 0")
    @DecimalMax(value = "1.0", message = "tasa no puede exceder 100%")
    private BigDecimal tasa;
}
