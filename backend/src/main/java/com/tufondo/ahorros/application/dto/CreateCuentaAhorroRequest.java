// com/tufondo/ahorros/application/dto/CreateCuentaAhorroRequest.java
package com.tufondo.ahorros.application.dto;

import com.tufondo.ahorros.domain.model.enums.Moneda;
import com.tufondo.ahorros.domain.model.enums.TipoCuenta;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO para crear una cuenta de ahorro.
 * Validaciones según SPEC.md.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCuentaAhorroRequest {
    
    @NotNull(message = "socioId es requerido")
    private UUID socioId;
    
    @NotNull(message = "tipoCuenta es requerido")
    private TipoCuenta tipoCuenta;
    
    @NotNull(message = "moneda es requerida")
    private Moneda moneda;
    
    @DecimalMin(value = "0.0001", message = "montoMinimoRequerido debe ser >= 0.0001")
    @DecimalMax(value = "999999999.9999", message = "montoMinimoRequerido excede límite máximo")
    private BigDecimal montoMinimoRequerido;
    
    @DecimalMin(value = "0.0001", message = "tasaInteres debe ser >= 0.0001 (RN-010)")
    @DecimalMax(value = "1.0", message = "tasaInteres no puede exceder 100%")
    private BigDecimal tasaInteres;
}