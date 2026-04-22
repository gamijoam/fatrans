// com.tufondo.kyc.infrastructure.persistence.adapter.ConsentimientoKYCRepositoryImpl
package com.tufondo.kyc.infrastructure.persistence.adapter;

import com.tufondo.kyc.domain.model.ConsentimientoKYC;
import com.tufondo.kyc.domain.repository.ConsentimientoKYCRepository;
import com.tufondo.kyc.infrastructure.persistence.entity.ConsentimientoKYCEntity;
import com.tufondo.kyc.infrastructure.persistence.jpa.ConsentimientoKYCJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ConsentimientoKYCRepositoryImpl implements ConsentimientoKYCRepository {

    private final ConsentimientoKYCJpaRepository jpaRepository;

    @Override
    public Optional<ConsentimientoKYC> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<ConsentimientoKYC> findBySocioIdOrderByFechaConsentimientoDesc(UUID socioId) {
        return jpaRepository.findBySocioIdOrderByFechaConsentimientoDesc(socioId).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<ConsentimientoKYC> findLatestBySocioId(UUID socioId) {
        return jpaRepository.findFirstBySocioIdOrderByFechaConsentimientoDesc(socioId)
            .map(this::toDomain);
    }

    @Override
    public Optional<ConsentimientoKYC> findActiveBySocioId(UUID socioId) {
        // Buscar consentimiento donde aceptado = true y más reciente
        return jpaRepository.findFirstBySocioIdAndAceptadoTrueOrderByFechaConsentimientoDesc(socioId)
            .map(this::toDomain);
    }

    @Override
    public boolean existsBySocioIdAndAceptadoTrue(UUID socioId) {
        return jpaRepository.existsBySocioIdAndAceptadoTrue(socioId);
    }

    @Override
    public ConsentimientoKYC save(ConsentimientoKYC consentimiento) {
        ConsentimientoKYCEntity entity = toEntity(consentimiento);
        entity = jpaRepository.save(entity);
        return toDomain(entity);
    }

    private ConsentimientoKYC toDomain(ConsentimientoKYCEntity entity) {
        return ConsentimientoKYC.builder()
            .id(entity.getId())
            .socioId(entity.getSocioId())
            .tipoConsentimiento(entity.getTipoConsentimiento())
            .aceptado(entity.getAceptado())
            .fechaConsentimiento(entity.getFechaConsentimiento())
            .ipCliente(entity.getIpCliente())
            .userAgent(entity.getUserAgent())
            .versionPolitica(entity.getVersionPolitica())
            .build();
    }

    private ConsentimientoKYCEntity toEntity(ConsentimientoKYC domain) {
        return ConsentimientoKYCEntity.builder()
            .id(domain.getId())
            .socioId(domain.getSocioId())
            .tipoConsentimiento(domain.getTipoConsentimiento())
            .aceptado(domain.isAceptado())
            .fechaConsentimiento(domain.getFechaConsentimiento())
            .ipCliente(domain.getIpCliente())
            .userAgent(domain.getUserAgent())
            .versionPolitica(domain.getVersionPolitica())
            .build();
    }
}