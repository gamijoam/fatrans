// com.tufondo.kyc.infrastructure.persistence.entity.DocumentoIdentidadEntity
package com.tufondo.kyc.infrastructure.persistence.entity;

import com.tufondo.kyc.domain.model.enums.EstadoDocumento;
import com.tufondo.kyc.domain.model.enums.TipoDocumentoKYC;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity para DocumentoIdentidad.
 */
@Entity
@Table(name = "documento_identidad",
    indexes = {
        @Index(name = "idx_documento_verificacion_id", columnList = "verificacion_id"),
        @Index(name = "idx_documento_socio_id", columnList = "socio_id"),
        @Index(name = "idx_documento_tipo", columnList = "tipo_documento"),
        @Index(name = "idx_documento_estado", columnList = "estado")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_documento_tipo_verificacion", columnNames = {"verificacion_id", "tipo_documento"})
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentoIdentidadEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "verificacion_id", nullable = false)
    private UUID verificacionId;

    @Column(name = "socio_id", nullable = false)
    private UUID socioId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false, length = 30)
    private TipoDocumentoKYC tipoDocumento;

    @Column(name = "url_almacenamiento", nullable = false, length = 500)
    private String urlAlmacenamiento;

    @Column(name = "nombre_original", nullable = false, length = 255)
    private String nombreOriginal;

    @Column(name = "tamano_bytes", nullable = false)
    private Long tamanoBytes;

    @Column(name = "mime_type", nullable = false, length = 50)
    private String mimeType;

    @Column(name = "hash_archivo", nullable = false, length = 64)
    private String hashArchivo;

    @Column(name = "fecha_subida", nullable = false)
    private LocalDateTime fechaSubida;

    @Column(name = "fecha_expiracion_documento")
    private LocalDate fechaExpiracionDocumento;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoDocumento estado;

    @Column(name = "motivo_rechazo", columnDefinition = "TEXT")
    private String motivoRechazo;

    @Column(name = "metadatos_validacion", columnDefinition = "TEXT")
    private String metadatosValidacion;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;
}