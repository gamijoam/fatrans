// com/tufondo/creditos/application/dto/CuotaResponse.java
package com.tufondo.creditos.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de respuesta para cuota de amortización.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuotaResponse {
    private String id;
    private Integer numeroCuota;
    private LocalDate fechaVencimiento;
    private LocalDate fechaPago;
    private BigDecimal capital;
    private BigDecimal interes;
    private BigDecimal montoCuota;
    private BigDecimal saldoInsoluto;
    private String estado;
    private Integer diasMora;
    private BigDecimal interesMora;
    private BigDecimal montoPagado;
}
