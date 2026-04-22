// com.tufondo.kyc.domain.repository.DocumentoIdentidadRepository
package com.tufondo.kyc.domain.repository;

import com.tufondo.kyc.domain.model.DocumentoIdentidad;
import com.tufondo.kyc.domain.model.enums.EstadoDocumento;
import com.tufondo.kyc.domain.model.enums.TipoDocumentoKYC;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface para DocumentoIdentidad.
 */
public interface DocumentoIdentidadRepository {

    Optional<DocumentoIdentidad> findById(UUID id);

    List<DocumentoIdentidad> findByVerificacionId(UUID verificacionId);

    List<DocumentoIdentidad> findBySocioId(UUID socioId);

    Optional<DocumentoIdentidad> findByVerificacionIdAndTipo(UUID verificacionId, TipoDocumentoKYC tipo);

    List<DocumentoIdentidad> findByVerificacionIdAndEstado(UUID verificacionId, EstadoDocumento estado);

    boolean existsByVerificacionIdAndTipo(UUID verificacionId, TipoDocumentoKYC tipo);

    Long countByVerificacionIdAndEstado(UUID verificacionId, EstadoDocumento estado);

    Long countByVerificacionId(UUID verificacionId);

    DocumentoIdentidad save(DocumentoIdentidad documento);

    void delete(UUID id);

    void deleteByVerificacionId(UUID verificacionId);
}