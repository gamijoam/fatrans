// com/tufondo/ahorros/application/dto/CuentasPorSocioResponse.java
package com.tufondo.ahorros.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO para listar cuentas por socio.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuentasPorSocioResponse {
    private UUID socioId;
    private int totalCuentas;
    private List<CuentaResumen> cuentas;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CuentaResumen {
        private UUID id;
        private String numeroCuenta;
        private java.math.BigDecimal saldoActual;
        private String estado;
        private String tipoCuenta;
        private String moneda;
        private java.time.LocalDateTime fechaApertura;
    }
}