// com.tufondo.documentospdf.infrastructure.persistence.jpa.DocumentoJpaRepository
package com.tufondo.documentospdf.infrastructure.persistence.jpa;

import com.tufondo.documentospdf.domain.model.enums.EstadoDocumento;
import com.tufondo.documentospdf.domain.model.enums.TipoDocumento;
import com.tufondo.documentospdf.infrastructure.persistence.entity.DocumentoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repositorio JPA para DocumentoEntity.
 */
@Repository
public interface DocumentoJpaRepository extends JpaRepository<DocumentoEntity, UUID> {

    /**
     * Lista documentos por socio con paginación.
     */
    Page<DocumentoEntity> findBySocioIdOrderByFechaGeneracionDesc(UUID socioId, Pageable pageable);

    /**
     * Lista documentos por socio y tipo.
     */
    Page<DocumentoEntity> findBySocioIdAndTipoOrderByFechaGeneracionDesc(UUID socioId, TipoDocumento tipo, Pageable pageable);

    /**
     * Lista documentos por socio y estado.
     */
    Page<DocumentoEntity> findBySocioIdAndEstadoOrderByFechaGeneracionDesc(UUID socioId, EstadoDocumento estado, Pageable pageable);

    /**
     * Cuenta documentos por socio.
     */
    long countBySocioId(UUID socioId);

    /**
     * Cuenta documentos por socio y tipo.
     */
    long countBySocioIdAndTipo(UUID socioId, TipoDocumento tipo);

    /**
     * Cuenta documentos por socio y estado.
     */
    long countBySocioIdAndEstado(UUID socioId, EstadoDocumento estado);

    /**
     * Actualiza el estado de un documento.
     */
    @Modifying
    @Query("UPDATE DocumentoEntity d SET d.estado = :estado, d.updatedAt = CURRENT_TIMESTAMP WHERE d.id = :id")
    void actualizarEstado(@Param("id") UUID id, @Param("estado") EstadoDocumento estado);

    /**
     * Busca documento por hash.
     */
    DocumentoEntity findByHashArchivo(String hashArchivo);
}
