// com.tufondo.kyc.infrastructure.persistence.jpa.DocumentoIdentidadJpaRepository
package com.tufondo.kyc.infrastructure.persistence.jpa;

import com.tufondo.kyc.domain.model.enums.EstadoDocumento;
import com.tufondo.kyc.domain.model.enums.TipoDocumentoKYC;
import com.tufondo.kyc.infrastructure.persistence.entity.DocumentoIdentidadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentoIdentidadJpaRepository extends JpaRepository<DocumentoIdentidadEntity, UUID> {

    List<DocumentoIdentidadEntity> findByVerificacionId(UUID verificacionId);

    List<DocumentoIdentidadEntity> findBySocioId(UUID socioId);

    Optional<DocumentoIdentidadEntity> findByVerificacionIdAndTipoDocumento(UUID verificacionId, TipoDocumentoKYC tipoDocumento);

    List<DocumentoIdentidadEntity> findByVerificacionIdAndEstado(UUID verificacionId, EstadoDocumento estado);

    boolean existsByVerificacionIdAndTipoDocumento(UUID verificacionId, TipoDocumentoKYC tipoDocumento);

    Long countByVerificacionIdAndEstado(UUID verificacionId, EstadoDocumento estado);

    Long countByVerificacionId(UUID verificacionId);

    void deleteByVerificacionId(UUID verificacionId);
}