package com.tufondo.kyc.infrastructure.persistence.entity;

import com.tufondo.kyc.domain.model.enums.EstadoIntentoBiometrico;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Los índices y constraints viven en la migración Flyway V13 — no se declaran aquí
 * para evitar duplicación cuando ddl-auto migre de update a validate.
 */
@Entity
@Table(name = "biometric_verification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificacionBiometricaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "verificacion_kyc_id", nullable = false)
    private UUID verificacionKycId;

    @Column(name = "socio_id", nullable = false)
    private UUID socioId;

    @Column(name = "proveedor", nullable = false, length = 50)
    private String proveedor;

    @Column(name = "proveedor_session_id", nullable = false, length = 255)
    private String proveedorSessionId;

    @Column(name = "proveedor_workflow_id", length = 100)
    private String proveedorWorkflowId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoIntentoBiometrico estado;

    @Column(name = "liveness_score", precision = 5, scale = 4)
    private BigDecimal livenessScore;

    @Column(name = "face_match_score", precision = 5, scale = 4)
    private BigDecimal faceMatchScore;

    @Column(name = "document_ocr_score", precision = 5, scale = 4)
    private BigDecimal documentOcrScore;

    @Column(name = "motivo_fallo", length = 500)
    private String motivoFallo;

    @Column(name = "tipo_ataque_detectado", length = 100)
    private String tipoAtaqueDetectado;

    @Column(name = "selfie_storage_path", length = 500)
    private String selfieStoragePath;

    @Column(name = "documento_storage_path", length = 500)
    private String documentoStoragePath;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_completado")
    private LocalDateTime fechaCompletado;

    @Column(name = "fecha_expiracion_artefactos")
    private LocalDateTime fechaExpiracionArtefactos;

    @Column(name = "ip_cliente", length = 45)
    private String ipCliente;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;
}
