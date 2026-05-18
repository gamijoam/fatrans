package com.tufondo.tipocambio.application.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoCambioRequest {

    @NotNull(message = "La fecha es requerida")
    private LocalDate fecha;

    @NotNull(message = "La tasa de compra es requerida")
    @DecimalMin(value = "0.000001", message = "La tasa de compra debe ser mayor a 0")
    @Digits(integer = 12, fraction = 6, message = "La tasa debe tener máximo 6 decimales")
    private BigDecimal tasaCompra;

    @NotNull(message = "La tasa de venta es requerida")
    @DecimalMin(value = "0.000001", message = "La tasa de venta debe ser mayor a 0")
    @Digits(integer = 12, fraction = 6, message = "La tasa debe tener máximo 6 decimales")
    private BigDecimal tasaVenta;

    @Size(max = 50, message = "La fuente no puede exceder 50 caracteres")
    private String fuente;
}