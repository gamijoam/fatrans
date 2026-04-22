// com.tufondo.documentospdf.infrastructure.persistence.entity.DocumentoEntity
package com.tufondo.documentospdf.infrastructure.persistence.entity;

import com.tufondo.documentospdf.domain.model.Documento;
import com.tufondo.documentospdf.domain.model.enums.ClasificacionDocumento;
import com.tufondo.documentospdf.domain.model.enums.EstadoDocumento;
import com.tufondo.documentospdf.domain.model.enums.TipoDocumento;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity JPA para Documento PDF.
 */
@Entity
@Table(name = "documentos_pdf",
    indexes = {
        @Index(name = "idx_documentos_socio_id", columnList = "socio_id"),
        @Index(name = "idx_documentos_tipo", columnList = "tipo"),
        @Index(name = "idx_documentos_estado", columnList = "estado"),
        @Index(name = "idx_documentos_fecha_gen", columnList = "fecha_generacion DESC"),
        @Index(name = "idx_documentos_socio_tipo", columnList = "socio_id, tipo"),
        @Index(name = "idx_documentos_hash", columnList = "hash_archivo")
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "socio_id", nullable = false)
    private UUID socioId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoDocumento tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoDocumento estado;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(name = "ruta_almacenamiento", nullable = false, columnDefinition = "TEXT")
    private String rutaAlmacenamiento;

    @Column(name = "hash_archivo", nullable = false, length = 71)
    private String hashArchivo;

    @Column(name = "firma_digital", columnDefinition = "TEXT")
    private String firmaDigital;

    @Column(name = "tamano_bytes", nullable = false)
    private Long tamanoBytes;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    @Column(name = "fecha_expiracion")
    private LocalDateTime fechaExpiracion;

    @Column(name = "generado_por", nullable = false, length = 100)
    private String generadoPor;

    @Enumerated(EnumType.STRING)
    @Column(name = "clasificacion", nullable = false, length = 20)
    private ClasificacionDocumento clasificacion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Conversión a dominio
    public Documento toDomain() {
        return Documento.builder()
                .id(this.id)
                .socioId(this.socioId)
                .tipo(this.tipo)
                .estado(this.estado)
                .nombreArchivo(this.nombreArchivo)
                .rutaAlmacenamiento(this.rutaAlmacenamiento)
                .hashArchivo(this.hashArchivo)
                .firmaDigital(this.firmaDigital)
                .tamanoBytes(this.tamanoBytes)
                .fechaGeneracion(this.fechaGeneracion)
                .fechaExpiracion(this.fechaExpiracion)
                .generadoPor(this.generadoPor)
                .clasificacion(this.clasificacion)
                .build();
    }

    // Factory desde dominio
    public static DocumentoEntity fromDomain(Documento documento) {
        return DocumentoEntity.builder()
                .id(documento.getId())
                .socioId(documento.getSocioId())
                .tipo(documento.getTipo())
                .estado(documento.getEstado())
                .nombreArchivo(documento.getNombreArchivo())
                .rutaAlmacenamiento(documento.getRutaAlmacenamiento())
                .hashArchivo(documento.getHashArchivo())
                .firmaDigital(documento.getFirmaDigital())
                .tamanoBytes(documento.getTamanoBytes())
                .fechaGeneracion(documento.getFechaGeneracion())
                .fechaExpiracion(documento.getFechaExpiracion())
                .generadoPor(documento.getGeneradoPor())
                .clasificacion(documento.getClasificacion())
                .build();
    }
}
