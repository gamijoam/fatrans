// com/tufondo/ahorros/application/dto/RetiroRequest.java
package com.tufondo.ahorros.application.dto;

import com.tufondo.ahorros.domain.model.enums.CanalOrigen;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para realizar un retiro.
 * Límite: configurable por moneda.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RetiroRequest {
    
    @NotNull(message = "monto es requerido")
    @DecimalMin(value = "0.0001", message = "monto debe ser >= 0.0001")
    @DecimalMax(value = "50000.00", message = "monto excede límite de retiro diario")
    private BigDecimal monto;
    
    @NotNull(message = "canalOrigen es requerido")
    private CanalOrigen canalOrigen;
}