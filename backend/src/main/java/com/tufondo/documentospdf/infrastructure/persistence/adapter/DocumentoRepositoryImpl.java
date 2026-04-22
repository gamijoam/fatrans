// com.tufondo.documentospdf.infrastructure.persistence.adapter.DocumentoRepositoryImpl
package com.tufondo.documentospdf.infrastructure.persistence.adapter;

import com.tufondo.documentospdf.domain.model.Documento;
import com.tufondo.documentospdf.domain.model.enums.EstadoDocumento;
import com.tufondo.documentospdf.domain.model.enums.TipoDocumento;
import com.tufondo.documentospdf.domain.repository.DocumentoRepository;
import com.tufondo.documentospdf.infrastructure.persistence.entity.DocumentoEntity;
import com.tufondo.documentospdf.infrastructure.persistence.jpa.DocumentoJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementación del repositorio de documentos.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentoRepositoryImpl implements DocumentoRepository {

    private final DocumentoJpaRepository jpaRepository;

    @Override
    @Transactional
    public Documento guardar(Documento documento) {
        DocumentoEntity entity = DocumentoEntity.fromDomain(documento);
        entity = jpaRepository.save(entity);
        log.debug("Documento guardado: id={}", entity.getId());
        return entity.toDomain();
    }

    @Override
    public Optional<Documento> buscarPorId(UUID id) {
        return jpaRepository.findById(id).map(DocumentoEntity::toDomain);
    }

    @Override
    public List<Documento> listarPorSocio(UUID socioId, int page, int size) {
        return jpaRepository.findBySocioIdOrderByFechaGeneracionDesc(socioId, PageRequest.of(page, size))
                .getContent()
                .stream()
                .map(DocumentoEntity::toDomain)
                .toList();
    }

    @Override
    public List<Documento> listarPorSocioYTipo(UUID socioId, TipoDocumento tipo, int page, int size) {
        return jpaRepository.findBySocioIdAndTipoOrderByFechaGeneracionDesc(socioId, tipo, PageRequest.of(page, size))
                .getContent()
                .stream()
                .map(DocumentoEntity::toDomain)
                .toList();
    }

    @Override
    public List<Documento> listarPorSocioYEstado(UUID socioId, EstadoDocumento estado, int page, int size) {
        return jpaRepository.findBySocioIdAndEstadoOrderByFechaGeneracionDesc(socioId, estado, PageRequest.of(page, size))
                .getContent()
                .stream()
                .map(DocumentoEntity::toDomain)
                .toList();
    }

    @Override
    public long contarPorSocio(UUID socioId) {
        return jpaRepository.countBySocioId(socioId);
    }

    @Override
    public long contarPorSocioYTipo(UUID socioId, TipoDocumento tipo) {
        return jpaRepository.countBySocioIdAndTipo(socioId, tipo);
    }

    @Override
    public long contarPorSocioYEstado(UUID socioId, EstadoDocumento estado) {
        return jpaRepository.countBySocioIdAndEstado(socioId, estado);
    }

    @Override
    @Transactional
    public void actualizarEstado(UUID id, EstadoDocumento estado) {
        jpaRepository.actualizarEstado(id, estado);
        log.debug("Estado actualizado: id={}, estado={}", id, estado);
    }
}
