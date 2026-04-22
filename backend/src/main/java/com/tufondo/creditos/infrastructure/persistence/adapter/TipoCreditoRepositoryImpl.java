// com/tufondo/creditos/infrastructure/persistence/adapter/TipoCreditoRepositoryImpl.java
package com.tufondo.creditos.infrastructure.persistence.adapter;

import com.tufondo.creditos.domain.model.TipoCredito;
import com.tufondo.creditos.domain.repository.TipoCreditoRepository;
import com.tufondo.creditos.infrastructure.persistence.entity.TipoCreditoEntity;
import com.tufondo.creditos.infrastructure.persistence.jpa.TipoCreditoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación del repositorio de TipoCredito.
 */
@Repository
@RequiredArgsConstructor
public class TipoCreditoRepositoryImpl implements TipoCreditoRepository {

    private final TipoCreditoJpaRepository jpaRepository;

    @Override
    public TipoCredito guardar(TipoCredito tipoCredito) {
        TipoCreditoEntity entity = toEntity(tipoCredito);
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }
        entity.setUpdatedAt(LocalDateTime.now());
        entity = jpaRepository.save(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<TipoCredito> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<TipoCredito> buscarPorIdActivo(Long id) {
        return jpaRepository.findByIdAndActivoTrue(id).map(this::toDomain);
    }

    @Override
    public Optional<TipoCredito> buscarPorCodigo(String codigo) {
        return jpaRepository.findByCodigo(codigo).map(this::toDomain);
    }

    @Override
    public List<TipoCredito> listarActivos() {
        return jpaRepository.findByActivoTrue().stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Page<TipoCredito> listarActivosPaginado(Pageable pageable) {
        return jpaRepository.findByActivoTrue(pageable).map(this::toDomain);
    }

    @Override
    public List<TipoCredito> listarTodos() {
        return jpaRepository.findAll().stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public boolean existePorCodigo(String codigo) {
        return jpaRepository.existsByCodigo(codigo);
    }

    private TipoCredito toDomain(TipoCreditoEntity entity) {
        return TipoCredito.builder()
            .id(entity.getId())
            .codigo(entity.getCodigo())
            .nombre(entity.getNombre())
            .descripcion(entity.getDescripcion())
            .tasaInteresAnual(entity.getTasaInteresAnual())
            .plazoMinimoMeses(entity.getPlazoMinimoMeses())
            .plazoMaximoMeses(entity.getPlazoMaximoMeses())
            .montoMinimo(entity.getMontoMinimo())
            .montoMaximo(entity.getMontoMaximo())
            .porcentajeRequerimientoColateral(entity.getPorcentajeRequerimientoColateral())
            .comisionApertura(entity.getComisionApertura())
            .penalidadMoraTasa(entity.getPenalidadMoraTasa())
            .diasGracia(entity.getDiasGracia())
            .activo(entity.getActivo())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .version(entity.getVersion())
            .build();
    }

    private TipoCreditoEntity toEntity(TipoCredito domain) {
        return TipoCreditoEntity.builder()
            .id(domain.getId())
            .codigo(domain.getCodigo())
            .nombre(domain.getNombre())
            .descripcion(domain.getDescripcion())
            .tasaInteresAnual(domain.getTasaInteresAnual())
            .plazoMinimoMeses(domain.getPlazoMinimoMeses())
            .plazoMaximoMeses(domain.getPlazoMaximoMeses())
            .montoMinimo(domain.getMontoMinimo())
            .montoMaximo(domain.getMontoMaximo())
            .porcentajeRequerimientoColateral(domain.getPorcentajeRequerimientoColateral())
            .comisionApertura(domain.getComisionApertura())
            .penalidadMoraTasa(domain.getPenalidadMoraTasa())
            .diasGracia(domain.getDiasGracia())
            .activo(domain.getActivo())
            .createdAt(domain.getCreatedAt())
            .updatedAt(domain.getUpdatedAt())
            .version(domain.getVersion())
            .build();
    }
}
