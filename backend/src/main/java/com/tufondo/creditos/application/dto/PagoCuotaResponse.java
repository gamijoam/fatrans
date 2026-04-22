// com/tufondo/creditos/application/dto/PagoCuotaResponse.java
package com.tufondo.creditos.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de respuesta para pago de cuota.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoCuotaResponse {
    private String id;
    private Integer numeroCuota;
    private String estado;
    private BigDecimal montoPagado;
    private LocalDate fechaPago;
    private String referenciaPago;
    private BigDecimal saldoInsolutoRestante;
    private String mensaje;
}
