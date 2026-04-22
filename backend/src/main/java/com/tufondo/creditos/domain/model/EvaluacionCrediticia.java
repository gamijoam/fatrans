// com/tufondo/creditos/domain/model/EvaluacionCrediticia.java
package com.tufondo.creditos.domain.model;

import com.tufondo.creditos.domain.model.enums.NivelRiesgo;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad EvaluacionCrediticia - Evaluación crediticia con auditoría criptográfica.
 * Incluye score hash SHA-256 y firma RSA para inmutabilidad.
 * 
 * Score interno: 0-100 pts = puntajeAntiguedad + puntajeHistorialAhorro + puntajeCapacidadPago
 * - Antigüedad: hasta 30 pts
 * - Historial ahorro: hasta 30 pts
 * - Capacidad pago: hasta 40 pts
 */
@Getter
@Setter
@Builder
public class EvaluacionCrediticia {
    private UUID id;
    private UUID solicitudId;
    private UUID socioId;
    
    // Factores de evaluación
    private Integer puntajeAntiguedad;  // 0-30 pts
    private Integer puntajeHistorialAhorro;  // 0-30 pts
    private Integer puntajeCapacidadPago;  // 0-40 pts
    
    // Score calculado
    private Integer scoreInterno;  // 0-100 pts
    
    // Auditoría criptográfica
    private String scoreHash;  // SHA-256 del cálculo completo
    private String factoresSerializados;  // JSON con breakdown del score
    private String firmaVerificable;  // Firma RSA (opcional)
    private UUID evaluacionIdOriginal;  // Para detectar modificaciones
    
    // Resultado
    private Boolean elegible;
    private NivelRiesgo nivelRiesgo;
    private BigDecimal tasaInteresFinal;
    private String mensajeDecision;
    private String evaluador;  // Admin o "SISTEMA"
    
    private LocalDateTime createdAt;
    private Long version;

    /**
     * Calcula el score interno como suma de los 3 factores.
     */
    public Integer calcularScore() {
        int score = 0;
        if (puntajeAntiguedad != null) score += Math.min(30, puntajeAntiguedad);
        if (puntajeHistorialAhorro != null) score += Math.min(30, puntajeHistorialAhorro);
        if (puntajeCapacidadPago != null) score += Math.min(40, puntajeCapacidadPago);
        return Math.min(100, score);
    }

    /**
     * Calcula y establece el hash SHA-256 del score.
     */
    public void calcularHash() {
        String factores = String.format("%d|%d|%d|%d|%s",
            puntajeAntiguedad != null ? puntajeAntiguedad : 0,
            puntajeHistorialAhorro != null ? puntajeHistorialAhorro : 0,
            puntajeCapacidadPago != null ? puntajeCapacidadPago : 0,
            scoreInterno != null ? scoreInterno : 0,
            socioId != null ? socioId.toString() : "null"
        );
        this.scoreHash = sha256(factores);
        this.factoresSerializados = serializeFactores();
    }

    /**
     * Verifica la integridad del score comparando el hash.
     */
    public boolean verificarIntegridad() {
        String factoresOriginales = String.format("%d|%d|%d|%d|%s",
            puntajeAntiguedad != null ? puntajeAntiguedad : 0,
            puntajeHistorialAhorro != null ? puntajeHistorialAhorro : 0,
            puntajeCapacidadPago != null ? puntajeCapacidadPago : 0,
            scoreInterno != null ? scoreInterno : 0,
            socioId != null ? socioId.toString() : "null"
        );
        return this.scoreHash.equals(sha256(factoresOriginales));
    }

    /**
     * Determina el nivel de riesgo basado en el score.
     */
    public NivelRiesgo determinarNivelRiesgo() {
        if (scoreInterno == null) return NivelRiesgo.ALTO;
        if (scoreInterno >= 70) return NivelRiesgo.BAJO;
        if (scoreInterno >= 50) return NivelRiesgo.MEDIO;
        return NivelRiesgo.ALTO;
    }

    /**
     * Calcula la tasa de interés final basada en el score.
     * - Score >= 80: 15% descuento
     * - Score 70-79: 5% descuento
     * - Score 60-69: sin modificación
     * - Score 50-59: 10% recargo
     */
    public BigDecimal calcularTasaInteres(BigDecimal tasaBase) {
        if (scoreInterno == null || tasaBase == null) return tasaBase;
        
        return switch (scoreInterno.intValue()) {
            case 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100 -> 
                tasaBase.multiply(new BigDecimal("0.85"));  // 15% descuento
            case 70, 71, 72, 73, 74, 75, 76, 77, 78, 79 -> 
                tasaBase.multiply(new BigDecimal("0.95"));  // 5% descuento
            case 60, 61, 62, 63, 64, 65, 66, 67, 68, 69 -> 
                tasaBase;  // Sin modificación
            default -> tasaBase.multiply(new BigDecimal("1.10"));  // 10% recargo
        };
    }

    /**
     * Determina si el socio es elegible para el crédito.
     * RN-E-01: Score >= 50
     */
    public boolean esElegible() {
        return scoreInterno != null && scoreInterno >= 50;
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error calculando SHA-256", e);
        }
    }

    private String serializeFactores() {
        return String.format(
            "{\"puntajeAntiguedad\":%d,\"puntajeHistorialAhorro\":%d,\"puntajeCapacidadPago\":%d,\"scoreTotal\":%d}",
            puntajeAntiguedad != null ? puntajeAntiguedad : 0,
            puntajeHistorialAhorro != null ? puntajeHistorialAhorro : 0,
            puntajeCapacidadPago != null ? puntajeCapacidadPago : 0,
            scoreInterno != null ? scoreInterno : 0
        );
    }
}
