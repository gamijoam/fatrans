// com/tufondo/ahorros/application/dto/CalcularRendimientoRequest.java
package com.tufondo.ahorros.application.dto;

import com.tufondo.ahorros.domain.model.enums.TipoRendimiento;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para calcular rendimiento.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalcularRendimientoRequest {
    
    @NotNull(message = "periodoInicio es requerido")
    private LocalDate periodoInicio;
    
    @NotNull(message = "periodoFin es requerido")
    private LocalDate periodoFin;
    
    @NotNull(message = "tipo es requerido")
    private TipoRendimiento tipo;
    
    @AssertTrue(message = "periodoFin debe ser mayor o igual a periodoInicio")
    public boolean isPeriodoValido() {
        return periodoFin == null || periodoInicio == null || !periodoFin.isBefore(periodoInicio);
    }
}