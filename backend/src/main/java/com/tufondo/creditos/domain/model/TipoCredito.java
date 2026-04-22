// com/tufondo/creditos/domain/model/TipoCredito.java
package com.tufondo.creditos.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad TipoCredito - Catálogo de productos crediticios.
 * Define las características de cada tipo de crédito oferecido.
 */
@Getter
@Builder
public class TipoCredito {
    private Long id;
    private String codigo;  // IDENTIFICADOR_UNICO, MICRO_CREDITO, CREDITO_VEHICULO
    private String nombre;
    private String descripcion;
    private BigDecimal tasaInteresAnual;
    private Integer plazoMinimoMeses;
    private Integer plazoMaximoMeses;
    private BigDecimal montoMinimo;
    private BigDecimal montoMaximo;
    private BigDecimal porcentajeRequerimientoColateral;  // % del monto como colateral
    private BigDecimal comisionApertura;
    private BigDecimal penalidadMoraTasa;  // Tasa diaria de penalización
    private Integer diasGracia;  // Días antes de marcar vencida
    private Boolean activo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;

    /**
     * Valida si un monto está dentro de los límites del tipo de crédito.
     */
    public boolean validaMonto(BigDecimal monto) {
        if (montoMinimo != null && monto.compareTo(montoMinimo) < 0) {
            return false;
        }
        if (montoMaximo != null && monto.compareTo(montoMaximo) > 0) {
            return false;
        }
        return true;
    }

    /**
     * Valida si un plazo está dentro de los límites del tipo de crédito.
     */
    public boolean validaPlazo(Integer plazo) {
        if (plazoMinimoMeses != null && plazo < plazoMinimoMeses) {
            return false;
        }
        if (plazoMaximoMeses != null && plazo > plazoMaximoMeses) {
            return false;
        }
        return true;
    }

    /**
     * Verifica si el tipo de crédito requiere colateral.
     */
    public boolean requiereColateral() {
        return porcentajeRequerimientoColateral != null && 
               porcentajeRequerimientoColateral.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Calcula el monto de colateral requerido para un monto de crédito dado.
     */
    public BigDecimal calcularColateralRequerido(BigDecimal montoCredito) {
        if (!requiereColateral()) {
            return BigDecimal.ZERO;
        }
        return montoCredito.multiply(porcentajeRequerimientoColateral);
    }
}
