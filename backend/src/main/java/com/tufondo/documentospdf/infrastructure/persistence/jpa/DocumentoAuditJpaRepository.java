// com.tufondo.documentospdf.infrastructure.persistence.jpa.DocumentoAuditJpaRepository
package com.tufondo.documentospdf.infrastructure.persistence.jpa;

import com.tufondo.documentospdf.infrastructure.persistence.entity.DocumentoAuditEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repositorio JPA para auditoría de documentos.
 */
@Repository
public interface DocumentoAuditJpaRepository extends JpaRepository<DocumentoAuditEntity, UUID> {

    /**
     * Lista auditoría por documento.
     */
    Page<DocumentoAuditEntity> findByDocumentoIdOrderByFechaEventoDesc(UUID documentoId, Pageable pageable);

    /**
     * Lista auditoría por usuario.
     */
    Page<DocumentoAuditEntity> findByUsuarioIdOrderByFechaEventoDesc(String usuarioId, Pageable pageable);
}
