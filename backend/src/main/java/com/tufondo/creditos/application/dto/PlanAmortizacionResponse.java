// com/tufondo/creditos/application/dto/PlanAmortizacionResponse.java
package com.tufondo.creditos.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO de respuesta para plan de amortización.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanAmortizacionResponse {
    private String id;
    private String solicitudId;
    private BigDecimal montoPrincipal;
    private BigDecimal tasaInteres;
    private Integer plazoMeses;
    private String frecuenciaPago;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer numeroCuotas;
    private BigDecimal cuotaMensual;
    private BigDecimal totalIntereses;
    private BigDecimal totalPagado;
    private BigDecimal saldoPendiente;
    private String estado;
    private List<CuotaResponse> cuotas;
}
