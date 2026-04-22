package com.tufondo.creditos.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoCreditoPublicResponse {
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private BigDecimal tasaInteresAnual;
    private Integer plazoMinimoMeses;
    private Integer plazoMaximoMeses;
    private BigDecimal montoMinimo;
    private BigDecimal montoMaximo;
    private BigDecimal porcentajeRequerimientoColateral;
    private Integer diasGracia;
}