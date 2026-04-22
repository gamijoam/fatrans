// com/tufondo/ahorros/application/dto/RendimientoResponse.java
package com.tufondo.ahorros.application.dto;

import com.tufondo.ahorros.domain.model.enums.EstadoAplicacion;
import com.tufondo.ahorros.domain.model.enums.TipoRendimiento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para rendimiento.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RendimientoResponse {
    private UUID id;
    private UUID cuentaAhorroId;
    private LocalDate periodoInicio;
    private LocalDate periodoFin;
    private BigDecimal saldoPromedioPeriodo;
    private BigDecimal tasaAplicada;
    private BigDecimal montoRendimiento;
    private TipoRendimiento tipo;
    private EstadoAplicacion estadoAplicacion;
    private LocalDateTime fechaCalculo;
}