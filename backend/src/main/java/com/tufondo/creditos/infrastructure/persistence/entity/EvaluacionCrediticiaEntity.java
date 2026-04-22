// com/tufondo/creditos/infrastructure/persistence/entity/EvaluacionCrediticiaEntity.java
package com.tufondo.creditos.infrastructure.persistence.entity;

import com.tufondo.creditos.domain.model.enums.NivelRiesgo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity para EvaluacionCrediticia.
 * Incluye campos de auditoría criptográfica para score hash y firma.
 */
@Entity
@Table(name = "evaluaciones_crediticias",
    indexes = {
        @Index(name = "idx_evaluaciones_solicitud_id", columnList = "solicitud_id", unique = true),
        @Index(name = "idx_evaluaciones_socio_id", columnList = "socio_id"),
        @Index(name = "idx_evaluaciones_elegible", columnList = "elegible"),
        @Index(name = "idx_evaluaciones_score", columnList = "score_interno")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_evaluacion_solicitud", columnNames = {"solicitud_id"})
    })
public class EvaluacionCrediticiaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "solicitud_id", nullable = false, unique = true)
    private UUID solicitudId;

    @Column(name = "socio_id", nullable = false)
    private UUID socioId;

    @Column(name = "puntaje_antiguedad", nullable = false)
    private Integer puntajeAntiguedad;

    @Column(name = "puntaje_historial_ahorro", nullable = false)
    private Integer puntajeHistorialAhorro;

    @Column(name = "puntaje_capacidad_pago", nullable = false)
    private Integer puntajeCapacidadPago;

    @Column(name = "score_interno", nullable = false)
    private Integer scoreInterno;

    // Auditoría criptográfica
    @Column(name = "score_hash", length = 64, nullable = false)
    private String scoreHash;

    @Column(name = "factores_serializados", columnDefinition = "TEXT", nullable = false)
    private String factoresSerializados;

    @Column(name = "firma_verificable", length = 128)
    private String firmaVerificable;

    @Column(name = "evaluacion_id_original")
    private UUID evaluacionIdOriginal;

    @Column(name = "elegible", nullable = false)
    private Boolean elegible;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_riesgo", length = 20)
    private NivelRiesgo nivelRiesgo;

    @Column(name = "tasa_interes_final", precision = 8, scale = 4)
    private BigDecimal tasaInteresFinal;

    @Column(name = "mensaje_decision", length = 500)
    private String mensajeDecision;

    @Column(name = "evaluador", length = 100)
    private String evaluador;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Version
    private Long version;

    public EvaluacionCrediticiaEntity() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID v) { this.id = v; }
    public UUID getSolicitudId() { return solicitudId; }
    public void setSolicitudId(UUID v) { this.solicitudId = v; }
    public UUID getSocioId() { return socioId; }
    public void setSocioId(UUID v) { this.socioId = v; }
    public Integer getPuntajeAntiguedad() { return puntajeAntiguedad; }
    public void setPuntajeAntiguedad(Integer v) { this.puntajeAntiguedad = v; }
    public Integer getPuntajeHistorialAhorro() { return puntajeHistorialAhorro; }
    public void setPuntajeHistorialAhorro(Integer v) { this.puntajeHistorialAhorro = v; }
    public Integer getPuntajeCapacidadPago() { return puntajeCapacidadPago; }
    public void setPuntajeCapacidadPago(Integer v) { this.puntajeCapacidadPago = v; }
    public Integer getScoreInterno() { return scoreInterno; }
    public void setScoreInterno(Integer v) { this.scoreInterno = v; }
    public String getScoreHash() { return scoreHash; }
    public void setScoreHash(String v) { this.scoreHash = v; }
    public String getFactoresSerializados() { return factoresSerializados; }
    public void setFactoresSerializados(String v) { this.factoresSerializados = v; }
    public String getFirmaVerificable() { return firmaVerificable; }
    public void setFirmaVerificable(String v) { this.firmaVerificable = v; }
    public UUID getEvaluacionIdOriginal() { return evaluacionIdOriginal; }
    public void setEvaluacionIdOriginal(UUID v) { this.evaluacionIdOriginal = v; }
    public Boolean getElegible() { return elegible; }
    public void setElegible(Boolean v) { this.elegible = v; }
    public NivelRiesgo getNivelRiesgo() { return nivelRiesgo; }
    public void setNivelRiesgo(NivelRiesgo v) { this.nivelRiesgo = v; }
    public BigDecimal getTasaInteresFinal() { return tasaInteresFinal; }
    public void setTasaInteresFinal(BigDecimal v) { this.tasaInteresFinal = v; }
    public String getMensajeDecision() { return mensajeDecision; }
    public void setMensajeDecision(String v) { this.mensajeDecision = v; }
    public String getEvaluador() { return evaluador; }
    public void setEvaluador(String v) { this.evaluador = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
    public Long getVersion() { return version; }
    public void setVersion(Long v) { this.version = v; }

    public static EvaluacionCrediticiaEntityBuilder builder() { return new EvaluacionCrediticiaEntityBuilder(); }

    public static class EvaluacionCrediticiaEntityBuilder {
        private EvaluacionCrediticiaEntity e = new EvaluacionCrediticiaEntity();
        public EvaluacionCrediticiaEntityBuilder id(UUID v) { e.id = v; return this; }
        public EvaluacionCrediticiaEntityBuilder solicitudId(UUID v) { e.solicitudId = v; return this; }
        public EvaluacionCrediticiaEntityBuilder socioId(UUID v) { e.socioId = v; return this; }
        public EvaluacionCrediticiaEntityBuilder puntajeAntiguedad(Integer v) { e.puntajeAntiguedad = v; return this; }
        public EvaluacionCrediticiaEntityBuilder puntajeHistorialAhorro(Integer v) { e.puntajeHistorialAhorro = v; return this; }
        public EvaluacionCrediticiaEntityBuilder puntajeCapacidadPago(Integer v) { e.puntajeCapacidadPago = v; return this; }
        public EvaluacionCrediticiaEntityBuilder scoreInterno(Integer v) { e.scoreInterno = v; return this; }
        public EvaluacionCrediticiaEntityBuilder scoreHash(String v) { e.scoreHash = v; return this; }
        public EvaluacionCrediticiaEntityBuilder factoresSerializados(String v) { e.factoresSerializados = v; return this; }
        public EvaluacionCrediticiaEntityBuilder firmaVerificable(String v) { e.firmaVerificable = v; return this; }
        public EvaluacionCrediticiaEntityBuilder evaluacionIdOriginal(UUID v) { e.evaluacionIdOriginal = v; return this; }
        public EvaluacionCrediticiaEntityBuilder elegible(Boolean v) { e.elegible = v; return this; }
        public EvaluacionCrediticiaEntityBuilder nivelRiesgo(NivelRiesgo v) { e.nivelRiesgo = v; return this; }
        public EvaluacionCrediticiaEntityBuilder tasaInteresFinal(BigDecimal v) { e.tasaInteresFinal = v; return this; }
        public EvaluacionCrediticiaEntityBuilder mensajeDecision(String v) { e.mensajeDecision = v; return this; }
        public EvaluacionCrediticiaEntityBuilder evaluador(String v) { e.evaluador = v; return this; }
        public EvaluacionCrediticiaEntityBuilder createdAt(LocalDateTime v) { e.createdAt = v; return this; }
        public EvaluacionCrediticiaEntityBuilder version(Long v) { e.version = v; return this; }
        public EvaluacionCrediticiaEntity build() { return e; }
    }
}
