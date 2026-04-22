// com.tufondo.documentospdf.infrastructure.persistence.entity.DocumentoAuditEntity
package com.tufondo.documentospdf.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity JPA para auditoría de documentos PDF (Shadow Table).
 */
@Entity
@Table(name = "documentos_pdf_audit",
    indexes = {
        @Index(name = "idx_doc_audit_documento_id", columnList = "documento_id"),
        @Index(name = "idx_doc_audit_usuario_id", columnList = "usuario_id, fecha_evento DESC"),
        @Index(name = "idx_doc_audit_fecha", columnList = "fecha_evento DESC")
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentoAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "documento_id", nullable = false)
    private UUID documentoId;

    @Column(name = "entidad_tipo", nullable = false, length = 50)
    @Builder.Default
    private String entidadTipo = "DOCUMENTO";

    @Column(name = "accion", nullable = false, length = 30)
    private String accion; // GENERAR, DESCARGAR, REVOCAR, EXPIRAR

    @Column(name = "usuario_id", nullable = false, length = 100)
    private String usuarioId;

    @Column(name = "usuario_rol", nullable = false, length = 30)
    private String usuarioRol;

    @Column(name = "ip_cliente", nullable = false, length = 45)
    private String ipCliente;

    @Column(name = "documento_hash", nullable = false, length = 71)
    private String documentoHash;

    @Column(name = "resultado", nullable = false, length = 20)
    private String resultado; // EXITOSO, FALLIDO

    @Column(name = "razon_fallo", columnDefinition = "TEXT")
    private String razonFallo;

    @Column(name = "fecha_evento", nullable = false)
    private LocalDateTime fechaEvento;

    @PrePersist
    protected void onCreate() {
        if (fechaEvento == null) {
            fechaEvento = LocalDateTime.now();
        }
    }
}
