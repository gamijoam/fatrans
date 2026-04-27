package com.tufondo.tipocambio.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoCambioResponse {
    private UUID id;
    private LocalDate fecha;
    private BigDecimal tasaCompra;
    private BigDecimal tasaVenta;
    private String fuente;
    private UUID creadoPor;
    private Instant createdAt;
    private BigDecimal variacionPorcentual;
}