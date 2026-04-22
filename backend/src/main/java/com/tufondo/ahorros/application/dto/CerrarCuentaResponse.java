// com/tufondo/ahorros/application/dto/CerrarCuentaResponse.java
package com.tufondo.ahorros.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para respuesta de cierre de cuenta.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CerrarCuentaResponse {
    private UUID id;
    private String numeroCuenta;
    private String estado;
    private LocalDateTime fechaCierre;
    private BigDecimal saldoFinal;
    private String mensaje;
}