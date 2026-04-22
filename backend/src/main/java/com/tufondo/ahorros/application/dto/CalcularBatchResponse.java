// com/tufondo/ahorros/application/dto/CalcularBatchResponse.java
package com.tufondo.ahorros.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO para respuesta de cálculo batch.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalcularBatchResponse {
    private int totalCuentas;
    private int procesadas;
    private int exitosas;
    private int fallidas;
    private List<ResultadoCuenta> resultados;
    private LocalDateTime fechaProcesamiento;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResultadoCuenta {
        private UUID cuentaId;
        private String numeroCuenta;
        private boolean exitoso;
        private UUID rendimientoId;
        private String error;
    }
}