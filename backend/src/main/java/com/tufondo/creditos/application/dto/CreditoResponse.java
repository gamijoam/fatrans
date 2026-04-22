// com/tufondo/creditos/application/dto/CreditoResponse.java
package com.tufondo.creditos.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO de respuesta para consulta completa de crédito.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditoResponse {
    private String id;
    private String numeroSolicitud;
    private UUID socioId;
    private TipoCreditoResponse tipoCredito;
    private BigDecimal montoSolicitado;
    private Integer plazoMeses;
    private BigDecimal tasaInteresAplicada;
    private String estado;
    private BigDecimal colateralMontoRetenido;
    private String referenciaDesembolso;
    private LocalDateTime fechaDesembolso;
    private PlanAmortizacionResponse plan;
    private ResumenResponse resumen;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResumenResponse {
        private Integer cuotasPagadas;
        private Integer cuotasPendientes;
        private Integer cuotasVencidas;
        private BigDecimal totalIntereses;
        private BigDecimal totalPagadoIntereses;
    }
}
