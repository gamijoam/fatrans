// com/tufondo/creditos/application/dto/CrearSolicitudCreditoRequest.java
package com.tufondo.creditos.application.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para crear una solicitud de crédito.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearSolicitudCreditoRequest {
    
    @NotNull(message = "tipoCreditoId es requerido")
    private Long tipoCreditoId;
    
    @NotNull(message = "montoSolicitado es requerido")
    @DecimalMin(value = "0.0001", message = "monto debe ser >= 0.0001")
    @DecimalMax(value = "999999999.9999", message = "monto excede límite máximo")
    private BigDecimal montoSolicitado;
    
    @NotNull(message = "plazoMeses es requerido")
    @Min(value = 1, message = "plazo mínimo 1 mes")
    @Max(value = 360, message = "plazo máximo 360 meses")
    private Integer plazoMeses;
    
    @Size(max = 500, message = "destinoCredito no puede exceder 500 caracteres")
    private String destinoCredito;
    
    @Size(max = 34, message = "cuentaDestino no puede exceder 34 caracteres")
    private String cuentaDestino;
    
    private String colateralCuentaId;  // UUID como string

    private Long productoFinanciableId;
    private String productoNombreSnapshot;
    private BigDecimal productoPrecioSnapshot;
    private String productoMonedaSnapshot;
    private BigDecimal productoColateralRequeridoSnapshot;
}
