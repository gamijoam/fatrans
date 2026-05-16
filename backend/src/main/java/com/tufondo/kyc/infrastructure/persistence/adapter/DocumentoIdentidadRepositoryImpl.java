// com.tufondo.kyc.infrastructure.persistence.adapter.DocumentoIdentidadRepositoryImpl
package com.tufondo.kyc.infrastructure.persistence.adapter;

import com.tufondo.kyc.domain.model.DocumentoIdentidad;
import com.tufondo.kyc.domain.model.enums.EstadoDocumento;
import com.tufondo.kyc.domain.model.enums.TipoDocumentoKYC;
import com.tufondo.kyc.domain.repository.DocumentoIdentidadRepository;
import com.tufondo.kyc.infrastructure.persistence.entity.DocumentoIdentidadEntity;
import com.tufondo.kyc.infrastructure.persistence.jpa.DocumentoIdentidadJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class DocumentoIdentidadRepositoryImpl implements DocumentoIdentidadRepository {

    private final DocumentoIdentidadJpaRepository jpaRepository;

    @Override
    public Optional<DocumentoIdentidad> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<DocumentoIdentidad> findByVerificacionId(UUID verificacionId) {
        return jpaRepository.findByVerificacionId(verificacionId).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<DocumentoIdentidad> findBySocioId(UUID socioId) {
        return jpaRepository.findBySocioId(socioId).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<DocumentoIdentidad> findByVerificacionIdAndTipo(UUID verificacionId, TipoDocumentoKYC tipo) {
        return jpaRepository.findByVerificacionIdAndTipoDocumento(verificacionId, tipo)
            .map(this::toDomain);
    }

    @Override
    public List<DocumentoIdentidad> findByVerificacionIdAndEstado(UUID verificacionId, EstadoDocumento estado) {
        return jpaRepository.findByVerificacionIdAndEstado(verificacionId, estado).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public boolean existsByVerificacionIdAndTipo(UUID verificacionId, TipoDocumentoKYC tipo) {
        return jpaRepository.existsByVerificacionIdAndTipoDocumento(verificacionId, tipo);
    }

    @Override
    public Long countByVerificacionIdAndEstado(UUID verificacionId, EstadoDocumento estado) {
        return jpaRepository.countByVerificacionIdAndEstado(verificacionId, estado);
    }

    @Override
    public Long countByVerificacionId(UUID verificacionId) {
        return jpaRepository.countByVerificacionId(verificacionId);
    }

    @Override
    public DocumentoIdentidad save(DocumentoIdentidad documento) {
        DocumentoIdentidadEntity entity = toEntity(documento);
        // Si el entity ya existe en BD, rescatamos el `version` (@Version para
        // optimistic locking) y el createdAt original. El domain DocumentoIdentidad
        // no expone esos campos —son detalles de persistencia—, así que sin
        // esto Hibernate ve `version=null` en un detached entity y falla con
        // "uninitialized version value". Mismo patrón que aplicamos en
        // VerificacionKYCRepositoryImpl.save(). Esto ocurre típicamente cuando
        // el admin aprueba/rechaza un documento (cambio de estado), no en el
        // INSERT inicial.
        if (entity.getId() != null) {
            Optional<DocumentoIdentidadEntity> existingOpt = jpaRepository.findById(entity.getId());
            if (existingOpt.isPresent()) {
                DocumentoIdentidadEntity existing = existingOpt.get();
                entity.setVersion(existing.getVersion());
                if (entity.getCreatedAt() == null) {
                    entity.setCreatedAt(existing.getCreatedAt());
                }
            }
        }
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }
        entity.setUpdatedAt(LocalDateTime.now());
        DocumentoIdentidadEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteByVerificacionId(UUID verificacionId) {
        jpaRepository.deleteByVerificacionId(verificacionId);
    }

    private DocumentoIdentidad toDomain(DocumentoIdentidadEntity entity) {
        return DocumentoIdentidad.builder()
            .id(entity.getId())
            .verificacionId(entity.getVerificacionId())
            .socioId(entity.getSocioId())
            .tipoDocumento(entity.getTipoDocumento())
            .urlAlmacenamiento(entity.getUrlAlmacenamiento())
            .nombreOriginal(entity.getNombreOriginal())
            .tamanoBytes(entity.getTamanoBytes())
            .mimeType(entity.getMimeType())
            .hashArchivo(entity.getHashArchivo())
            .fechaSubida(entity.getFechaSubida())
            .fechaExpiracionDocumento(entity.getFechaExpiracionDocumento())
            .estado(entity.getEstado())
            .motivoRechazo(entity.getMotivoRechazo())
            .metadatosValidacion(entity.getMetadatosValidacion())
            .observaciones(entity.getObservaciones())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    private DocumentoIdentidadEntity toEntity(DocumentoIdentidad domain) {
        return DocumentoIdentidadEntity.builder()
            .id(domain.getId())
            .verificacionId(domain.getVerificacionId())
            .socioId(domain.getSocioId())
            .tipoDocumento(domain.getTipoDocumento())
            .urlAlmacenamiento(domain.getUrlAlmacenamiento())
            .nombreOriginal(domain.getNombreOriginal())
            .tamanoBytes(domain.getTamanoBytes())
            .mimeType(domain.getMimeType())
            .hashArchivo(domain.getHashArchivo())
            .fechaSubida(domain.getFechaSubida())
            .fechaExpiracionDocumento(domain.getFechaExpiracionDocumento())
            .estado(domain.getEstado())
            .motivoRechazo(domain.getMotivoRechazo())
            .metadatosValidacion(domain.getMetadatosValidacion())
            .observaciones(domain.getObservaciones())
            .createdAt(domain.getCreatedAt())
            .updatedAt(domain.getUpdatedAt())
            .build();
    }
}