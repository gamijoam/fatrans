// com/tufondo/ahorros/application/dto/CuentaAhorroResponse.java
package com.tufondo.ahorros.application.dto;

import com.tufondo.ahorros.domain.model.enums.EstadoCuenta;
import com.tufondo.ahorros.domain.model.enums.Moneda;
import com.tufondo.ahorros.domain.model.enums.TipoCuenta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para cuenta de ahorro.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuentaAhorroResponse {
    private UUID id;
    private String numeroCuenta;
    private UUID socioId;
    private BigDecimal saldoActual;
    private BigDecimal saldoRetenido;
    private BigDecimal saldoDisponible;
    private BigDecimal tasaInteres;
    private BigDecimal montoMinimoRequerido;
    private EstadoCuenta estado;
    private TipoCuenta tipoCuenta;
    private Moneda moneda;
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaUltimaOperacion;
}