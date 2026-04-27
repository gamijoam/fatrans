package com.tufondo.creditos.application.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoCreditoRequest {

    @NotBlank(message = "El código es requerido")
    @Size(max = 50, message = "El código no puede exceder 50 caracteres")
    private String codigo;

    @NotBlank(message = "El nombre es requerido")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;

    @NotNull(message = "La tasa de interés es requerida")
    @DecimalMin(value = "0.0", inclusive = false, message = "La tasa debe ser mayor a 0")
    @DecimalMax(value = "1.0", inclusive = false, message = "La tasa debe ser menor a 1 (100%)")
    private BigDecimal tasaInteresAnual;

    @NotNull(message = "El plazo mínimo es requerido")
    @Min(value = 1, message = "El plazo mínimo debe ser al menos 1 mes")
    private Integer plazoMinimoMeses;

    @NotNull(message = "El plazo máximo es requerido")
    @Min(value = 1, message = "El plazo máximo debe ser al menos 1 mes")
    private Integer plazoMaximoMeses;

    @NotNull(message = "El monto mínimo es requerido")
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto mínimo debe ser mayor a 0")
    private BigDecimal montoMinimo;

    @NotNull(message = "El monto máximo es requerido")
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto máximo debe ser mayor a 0")
    private BigDecimal montoMaximo;

    private BigDecimal porcentajeRequerimientoColateral;

    @DecimalMax(value = "1.0", message = "La comisión de apertura no puede exceder 100%")
    private BigDecimal comisionApertura;

    @DecimalMax(value = "1.0", message = "La penalidad de mora no puede exceder 100%")
    private BigDecimal penalidadMoraTasa;

    private Integer diasGracia;

    private Boolean activo = true;
}