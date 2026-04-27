package com.tufondo.tipocambio.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class TipoCambio {
    private UUID id;
    private LocalDate fecha;
    private BigDecimal tasaCompra;
    private BigDecimal tasaVenta;
    private String fuente;
    private UUID creadoPor;
    private Instant createdAt;

    public boolean isTasaValida() {
        return tasaCompra != null && tasaVenta != null
            && tasaCompra.compareTo(BigDecimal.ZERO) > 0
            && tasaVenta.compareTo(BigDecimal.ZERO) > 0
            && tasaCompra.compareTo(tasaVenta) <= 0;
    }

    public BigDecimal convertirABolivares(BigDecimal montoEnDolares) {
        if (montoEnDolares == null) return null;
        return montoEnDolares.multiply(tasaVenta);
    }

    public BigDecimal convertirADolares(BigDecimal montoEnBolivares) {
        if (montoEnBolivares == null) return null;
        if (tasaCompra.compareTo(BigDecimal.ZERO) == 0) return null;
        return montoEnBolivares.divide(tasaCompra, 6, java.math.RoundingMode.HALF_UP);
    }
}