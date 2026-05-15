package com.tufondo.kyc.domain.model;

import com.tufondo.kyc.domain.model.enums.EstadoIntentoBiometrico;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Intento biométrico (registro individual de la tabla {@code biometric_verification}).
 *
 * Inmutable salvo por las transiciones de estado controladas — {@link #aprobar()},
 * {@link #rechazar(String, String)}, {@link #expirar()}, {@link #cancelar()}.
 *
 * Diseño LOPDP-first:
 * <ul>
 *   <li>NO almacena ningún template biométrico ni embeddings.</li>
 *   <li>Solo guarda los scores numéricos (no reversibles a imagen).</li>
 *   <li>El selfie capturado se guarda en MinIO con TTL corto
 *       ({@code fechaExpiracionArtefactos}) y se borra automáticamente.</li>
 *   <li>Cuando el socio revoca su consentimiento biométrico, el use case de
 *       revocación borra los registros vinculados.</li>
 * </ul>
 */
@Getter
@Builder
public class VerificacionBiometrica {

    private UUID id;
    private UUID verificacionKycId;
    private UUID socioId;

    private String proveedor;             // "DIDIT", "AWS_REKOGNITION", etc.
    private String proveedorSessionId;
    private String proveedorWorkflowId;

    private EstadoIntentoBiometrico estado;

    private BigDecimal livenessScore;     // 0.00–1.00
    private BigDecimal faceMatchScore;    // 0.00–1.00
    private BigDecimal documentOcrScore;  // 0.00–1.00

    private String motivoFallo;
    private String tipoAtaqueDetectado;

    private String selfieStoragePath;
    private String documentoStoragePath;

    private LocalDateTime fechaInicio;
    private LocalDateTime fechaCompletado;
    private LocalDateTime fechaExpiracionArtefactos;

    private String ipCliente;
    private String userAgent;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;

    /**
     * Marca el intento como APROBADA. Solo válido desde PENDIENTE o EN_PROGRESO.
     */
    public VerificacionBiometrica aprobar(BigDecimal livenessScore,
                                          BigDecimal faceMatchScore,
                                          BigDecimal documentOcrScore) {
        if (estado != EstadoIntentoBiometrico.PENDIENTE && estado != EstadoIntentoBiometrico.EN_PROGRESO) {
            throw new IllegalStateException("No se puede aprobar un intento en estado " + estado);
        }
        return toBuilder()
                .estado(EstadoIntentoBiometrico.APROBADA)
                .livenessScore(livenessScore)
                .faceMatchScore(faceMatchScore)
                .documentOcrScore(documentOcrScore)
                .fechaCompletado(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Marca el intento como RECHAZADA con motivo. Si el proveedor detectó un ataque,
     * se persiste como evidencia para auditoría.
     */
    public VerificacionBiometrica rechazar(String motivo, String tipoAtaqueDetectado) {
        if (estado != EstadoIntentoBiometrico.PENDIENTE && estado != EstadoIntentoBiometrico.EN_PROGRESO) {
            throw new IllegalStateException("No se puede rechazar un intento en estado " + estado);
        }
        return toBuilder()
                .estado(EstadoIntentoBiometrico.RECHAZADA)
                .motivoFallo(motivo)
                .tipoAtaqueDetectado(tipoAtaqueDetectado)
                .fechaCompletado(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public VerificacionBiometrica expirar() {
        return toBuilder()
                .estado(EstadoIntentoBiometrico.EXPIRADA)
                .fechaCompletado(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public VerificacionBiometrica cancelar() {
        return toBuilder()
                .estado(EstadoIntentoBiometrico.CANCELADA)
                .fechaCompletado(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Returns a builder pre-populated with the current state. Mutaciones controladas
     * por los métodos de transición ({@link #aprobar}, {@link #rechazar}, etc.).
     */
    public VerificacionBiometricaBuilder toBuilder() {
        return VerificacionBiometrica.builder()
                .id(this.id)
                .verificacionKycId(this.verificacionKycId)
                .socioId(this.socioId)
                .proveedor(this.proveedor)
                .proveedorSessionId(this.proveedorSessionId)
                .proveedorWorkflowId(this.proveedorWorkflowId)
                .estado(this.estado)
                .livenessScore(this.livenessScore)
                .faceMatchScore(this.faceMatchScore)
                .documentOcrScore(this.documentOcrScore)
                .motivoFallo(this.motivoFallo)
                .tipoAtaqueDetectado(this.tipoAtaqueDetectado)
                .selfieStoragePath(this.selfieStoragePath)
                .documentoStoragePath(this.documentoStoragePath)
                .fechaInicio(this.fechaInicio)
                .fechaCompletado(this.fechaCompletado)
                .fechaExpiracionArtefactos(this.fechaExpiracionArtefactos)
                .ipCliente(this.ipCliente)
                .userAgent(this.userAgent)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .version(this.version);
    }
}
