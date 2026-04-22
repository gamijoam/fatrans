// com/tufondo/creditos/application/dto/PagoCuotaRequest.java
package com.tufondo.creditos.application.dto;

import com.tufondo.creditos.domain.model.enums.CanalOrigen;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para registrar el pago de una cuota.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoCuotaRequest {
    
    @NotNull(message = "monto es requerido")
    @DecimalMin(value = "0.0001", message = "monto debe ser >= 0.0001")
    private BigDecimal monto;
    
    @Size(max = 100, message = "referenciaPago no puede exceder 100 caracteres")
    private String referenciaPago;
    
    @NotNull(message = "canalOrigen es requerido")
    private CanalOrigen canalOrigen;
}