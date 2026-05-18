package com.tufondo.kyc.infrastructure.persistence.adapter;

import com.tufondo.kyc.domain.model.ConsentimientoBiometrico;
import com.tufondo.kyc.domain.repository.ConsentimientoBiometricoRepository;
import com.tufondo.kyc.infrastructure.persistence.entity.ConsentimientoBiometricoEntity;
import com.tufondo.kyc.infrastructure.persistence.jpa.ConsentimientoBiometricoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ConsentimientoBiometricoRepositoryImpl implements ConsentimientoBiometricoRepository {

    private final ConsentimientoBiometricoJpaRepository jpaRepository;

    @Override
    @Transactional
    public ConsentimientoBiometrico save(ConsentimientoBiometrico c) {
        return toDomain(jpaRepository.save(toEntity(c)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ConsentimientoBiometrico> findVigenteBySocioId(UUID socioId) {
        return jpaRepository.findVigenteBySocioId(socioId).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ConsentimientoBiometrico> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    private ConsentimientoBiometricoEntity toEntity(ConsentimientoBiometrico d) {
        return ConsentimientoBiometricoEntity.builder()
                .id(d.getId())
                .socioId(d.getSocioId())
                .versionPolitica(d.getVersionPolitica())
                .proveedorDestino(d.getProveedorDestino())
                .paisProcesamiento(d.getPaisProcesamiento())
                .aceptado(d.getAceptado())
                .fechaConsentimiento(d.getFechaConsentimiento())
                .fechaRevocacion(d.getFechaRevocacion())
                .ipCliente(d.getIpCliente())
                .userAgent(d.getUserAgent())
                .build();
    }

    private ConsentimientoBiometrico toDomain(ConsentimientoBiometricoEntity e) {
        return ConsentimientoBiometrico.builder()
                .id(e.getId())
                .socioId(e.getSocioId())
                .versionPolitica(e.getVersionPolitica())
                .proveedorDestino(e.getProveedorDestino())
                .paisProcesamiento(e.getPaisProcesamiento())
                .aceptado(e.getAceptado())
                .fechaConsentimiento(e.getFechaConsentimiento())
                .fechaRevocacion(e.getFechaRevocacion())
                .ipCliente(e.getIpCliente())
                .userAgent(e.getUserAgent())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
