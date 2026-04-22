// com/tufondo/ahorros/application/dto/SaldoResponse.java
package com.tufondo.ahorros.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para consulta de saldo.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaldoResponse {
    private String numeroCuenta;
    private BigDecimal saldoActual;
    private BigDecimal saldoRetenido;
    private BigDecimal saldoDisponible;
    private LocalDateTime fechaConsulta;
    private BigDecimal limiteDeposito;
    private BigDecimal limiteRetiroDiario;
    private BigDecimal retirosRealizadosHoy;
    private BigDecimal retirosRestantesHoy;
}