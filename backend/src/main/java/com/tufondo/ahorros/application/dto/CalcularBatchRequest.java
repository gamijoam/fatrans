// com/tufondo/ahorros/application/dto/CalcularBatchRequest.java
package com.tufondo.ahorros.application.dto;

import com.tufondo.ahorros.domain.model.enums.TipoRendimiento;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO para cálculo batch de rendimientos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalcularBatchRequest {

    @NotNull(message = "periodoInicio es requerido")
    private LocalDate periodoInicio;

    @NotNull(message = "periodoFin es requerido")
    private LocalDate periodoFin;

    @NotNull(message = "tipo es requerido")
    private TipoRendimiento tipo;

    @NotEmpty(message = "cuentaIds no puede estar vacío")
    @Size(max = 1000, message = "Máximo 1000 cuentas por batch")
    private List<UUID> cuentaIds;
}