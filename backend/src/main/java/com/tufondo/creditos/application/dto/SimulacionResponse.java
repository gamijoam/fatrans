// com/tufondo/creditos/application/dto/SimulacionResponse.java
package com.tufondo.creditos.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO de respuesta para simulación de crédito.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulacionResponse {
    private BigDecimal monto;
    private Integer plazoMeses;
    private BigDecimal tasaInteresAnual;
    private BigDecimal cuotaMensual;
    private BigDecimal totalIntereses;
    private BigDecimal totalAPagar;
    private List<CuotaSimulada> planSimulado;
    private String nota;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CuotaSimulada {
        private Integer numeroCuota;
        private String fechaVencimiento;
        private BigDecimal capital;
        private BigDecimal interes;
        private BigDecimal montoCuota;
        private BigDecimal saldoInsoluto;
    }
}
