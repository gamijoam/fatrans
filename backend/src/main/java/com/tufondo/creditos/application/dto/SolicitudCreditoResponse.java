// com/tufondo/creditos/application/dto/SolicitudCreditoResponse.java
package com.tufondo.creditos.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para solicitud de crédito.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudCreditoResponse {
    private String id;
    private String numeroSolicitud;
    private UUID socioId;
    private Long tipoCreditoId;
    private String tipoCreditoNombre;
    private BigDecimal montoSolicitado;
    private Integer plazoMeses;
    private BigDecimal tasaInteresAplicada;
    private BigDecimal cuotaMensualEstimada;
    private String estado;
    private String colateralCuentaId;
    private BigDecimal colateralMontoRetenido;
    private String destinoCredito;
    private EvaluacionResponse evaluacion;
    private LocalDateTime createdAt;
    private LocalDateTime fechaAprobacion;
    private LocalDateTime fechaRechazo;
    private String mensaje;
}
