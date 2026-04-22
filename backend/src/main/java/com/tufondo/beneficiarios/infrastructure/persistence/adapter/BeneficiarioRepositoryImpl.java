// com/tufondo/beneficiarios/infrastructure/persistence/adapter/BeneficiarioRepositoryImpl.java
package com.tufondo.beneficiarios.infrastructure.persistence.adapter;

import com.tufondo.beneficiarios.domain.model.Beneficiario;
import com.tufondo.beneficiarios.domain.model.enums.Parentesco;
import com.tufondo.beneficiarios.domain.model.enums.TipoDocumento;
import com.tufondo.beneficiarios.domain.repository.BeneficiarioRepository;
import com.tufondo.beneficiarios.infrastructure.persistence.entity.BeneficiarioEntity;
import com.tufondo.beneficiarios.infrastructure.persistence.jpa.BeneficiarioJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación del repositorio de Beneficiarios.
 */
@Repository
@RequiredArgsConstructor
public class BeneficiarioRepositoryImpl implements BeneficiarioRepository {

    private final BeneficiarioJpaRepository jpaRepository;

    @Override
    @Transactional
    public Beneficiario guardar(Beneficiario beneficiario) {
        BeneficiarioEntity entity = toEntity(beneficiario);
        BeneficiarioEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Beneficiario> buscarPorId(UUID id) {
        return jpaRepository.findById(id)
                .filter(BeneficiarioEntity::isActivo)
                .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Beneficiario> listarPorSocioId(UUID socioId) {
        return jpaRepository.findBySocioIdAndActivoTrue(socioId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public int countActivosPorSocioId(UUID socioId) {
        return jpaRepository.countBySocioIdAndActivoTrue(socioId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorDocumento(UUID socioId, TipoDocumento tipoDocumento, String numeroDocumento, UUID excludeId) {
        return jpaRepository.existePorDocumento(socioId, tipoDocumento, numeroDocumento, excludeId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal sumarPorcentajesPorSocioId(UUID socioId) {
        return jpaRepository.sumarPorcentajesPorSocioId(socioId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Beneficiario> buscarPorIdIncluyendoInactivos(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    private BeneficiarioEntity toEntity(Beneficiario beneficiario) {
        return BeneficiarioEntity.builder()
                .id(beneficiario.getId())
                .socioId(beneficiario.getSocioId())
                .nombreCompleto(beneficiario.getNombreCompleto())
                .numeroDocumento(beneficiario.getNumeroDocumento())
                .tipoDocumento(beneficiario.getTipoDocumento())
                .parentesco(beneficiario.getParentesco())
                .porcentaje(beneficiario.getPorcentaje())
                .telefono(beneficiario.getTelefono())
                .activo(beneficiario.isActivo())
                .fechaRegistro(beneficiario.getFechaRegistro())
                .fechaActualizacion(beneficiario.getFechaActualizacion())
                .build();
    }

    private Beneficiario toDomain(BeneficiarioEntity entity) {
        return Beneficiario.builder()
                .id(entity.getId())
                .socioId(entity.getSocioId())
                .nombreCompleto(entity.getNombreCompleto())
                .numeroDocumento(entity.getNumeroDocumento())
                .tipoDocumento(entity.getTipoDocumento())
                .parentesco(entity.getParentesco())
                .porcentaje(entity.getPorcentaje())
                .telefono(entity.getTelefono())
                .activo(entity.isActivo())
                .fechaRegistro(entity.getFechaRegistro())
                .fechaActualizacion(entity.getFechaActualizacion())
                .build();
    }
}