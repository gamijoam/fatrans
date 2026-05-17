// com/tufondo/ahorros/application/dto/DepositoRequest.java
package com.tufondo.ahorros.application.dto;

import com.tufondo.ahorros.domain.model.enums.CanalOrigen;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para realizar un depósito.
 * Límite: configurable por moneda.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositoRequest {
    
    @NotNull(message = "monto es requerido")
    @DecimalMin(value = "0.0001", message = "monto debe ser >= 0.0001")
    @DecimalMax(value = "500000.00", message = "monto excede límite de depósito")
    private BigDecimal monto;
    
    @Size(max = 500, message = "descripcion no puede exceder 500 caracteres")
    private String descripcion;
    
    @Size(max = 100, message = "referencia no puede exceder 100 caracteres")
    private String referencia;
    
    @NotNull(message = "canalOrigen es requerido")
    private CanalOrigen canalOrigen;

    // ==== LOCDOFT — declaración jurada para operaciones grandes (#218 PR-C) ====
    //
    // Si el monto supera el umbral configurado en `parametros_sistema`, el
    // backend EXIGE que `confirmaOrigenLicito=true`. El frontend muestra un
    // modal con la pregunta y, al confirmar, reintenta el POST con este flag.
    // Si el monto NO supera el umbral, estos campos se ignoran.

    private Boolean confirmaOrigenLicito;

    @Size(max = 2000, message = "origenFondos no puede exceder 2000 caracteres")
    private String origenFondos;
}