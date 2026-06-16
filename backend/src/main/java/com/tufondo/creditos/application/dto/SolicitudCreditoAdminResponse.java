// com/tufondo/creditos/application/dto/SolicitudCreditoAdminResponse.java
package com.tufondo.creditos.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para solicitud de crédito con datos del socio (para Admin).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudCreditoAdminResponse {
    private String id;
    private String numeroSolicitud;
    private UUID socioId;
    private String socioNombre;
    private String socioNumero;
    private String socioCedula;
    private String socioCorreo;
    private String socioEmpresa;
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
    private Long productoFinanciableId;
    private String productoNombreSnapshot;
    private BigDecimal productoPrecioSnapshot;
    private String productoMonedaSnapshot;
    private BigDecimal productoColateralRequeridoSnapshot;
    private LocalDateTime createdAt;
    private LocalDateTime fechaAprobacion;
    private LocalDateTime fechaRechazo;
    private String motivoRechazo;
}
