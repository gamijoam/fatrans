// com/tufondo/creditos/application/dto/EvaluacionResponse.java
package com.tufondo.creditos.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de respuesta para evaluación crediticia.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluacionResponse {
    private String id;
    private String solicitudId;
    private UUID socioId;
    private Integer puntajeAntiguedad;
    private Integer puntajeHistorialAhorro;
    private Integer puntajeCapacidadPago;
    private Integer scoreInterno;
    private String scoreHash;
    private Boolean elegible;
    private String nivelRiesgo;
    private BigDecimal tasaInteresFinal;
    private String mensajeDecision;
    private String evaluador;
}
