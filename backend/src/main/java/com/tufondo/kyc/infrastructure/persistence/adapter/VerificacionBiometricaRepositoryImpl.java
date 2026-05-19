package com.tufondo.kyc.infrastructure.persistence.adapter;

import com.tufondo.kyc.domain.model.VerificacionBiometrica;
import com.tufondo.kyc.domain.repository.VerificacionBiometricaRepository;
import com.tufondo.kyc.infrastructure.persistence.entity.VerificacionBiometricaEntity;
import com.tufondo.kyc.infrastructure.persistence.jpa.VerificacionBiometricaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class VerificacionBiometricaRepositoryImpl implements VerificacionBiometricaRepository {

    private final VerificacionBiometricaJpaRepository jpaRepository;

    @Override
    @Transactional
    public VerificacionBiometrica save(VerificacionBiometrica verificacion) {
        VerificacionBiometricaEntity saved = jpaRepository.save(toEntity(verificacion));
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VerificacionBiometrica> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VerificacionBiometrica> findByProveedorSessionId(String proveedor, String sessionId) {
        return jpaRepository.findByProveedorAndProveedorSessionId(proveedor, sessionId).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VerificacionBiometrica> findByVerificacionKycId(UUID verificacionKycId) {
        return jpaRepository.findByVerificacionKycId(verificacionKycId).stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VerificacionBiometrica> findBySocioId(UUID socioId) {
        return jpaRepository.findBySocioId(socioId).stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VerificacionBiometrica> findLastBySocioId(UUID socioId) {
        return jpaRepository.findFirstBySocioIdOrderByFechaInicioDesc(socioId).map(this::toDomain);
    }

    @Override
    @Transactional
    public void deleteAllBySocioId(UUID socioId) {
        jpaRepository.deleteAllBySocioId(socioId);
    }

    private VerificacionBiometricaEntity toEntity(VerificacionBiometrica d) {
        return VerificacionBiometricaEntity.builder()
                .id(d.getId())
                .verificacionKycId(d.getVerificacionKycId())
                .socioId(d.getSocioId())
                .proveedor(d.getProveedor())
                .proveedorSessionId(d.getProveedorSessionId())
                .proveedorWorkflowId(d.getProveedorWorkflowId())
                .estado(d.getEstado())
                .livenessScore(d.getLivenessScore())
                .faceMatchScore(d.getFaceMatchScore())
                .documentOcrScore(d.getDocumentOcrScore())
                .motivoFallo(d.getMotivoFallo())
                .tipoAtaqueDetectado(d.getTipoAtaqueDetectado())
                .selfieStoragePath(d.getSelfieStoragePath())
                .documentoStoragePath(d.getDocumentoStoragePath())
                .fechaInicio(d.getFechaInicio())
                .fechaCompletado(d.getFechaCompletado())
                .fechaExpiracionArtefactos(d.getFechaExpiracionArtefactos())
                .ipCliente(d.getIpCliente())
                .userAgent(d.getUserAgent())
                .version(d.getVersion())
                .build();
    }

    private VerificacionBiometrica toDomain(VerificacionBiometricaEntity e) {
        return VerificacionBiometrica.builder()
                .id(e.getId())
                .verificacionKycId(e.getVerificacionKycId())
                .socioId(e.getSocioId())
                .proveedor(e.getProveedor())
                .proveedorSessionId(e.getProveedorSessionId())
                .proveedorWorkflowId(e.getProveedorWorkflowId())
                .estado(e.getEstado())
                .livenessScore(e.getLivenessScore())
                .faceMatchScore(e.getFaceMatchScore())
                .documentOcrScore(e.getDocumentOcrScore())
                .motivoFallo(e.getMotivoFallo())
                .tipoAtaqueDetectado(e.getTipoAtaqueDetectado())
                .selfieStoragePath(e.getSelfieStoragePath())
                .documentoStoragePath(e.getDocumentoStoragePath())
                .fechaInicio(e.getFechaInicio())
                .fechaCompletado(e.getFechaCompletado())
                .fechaExpiracionArtefactos(e.getFechaExpiracionArtefactos())
                .ipCliente(e.getIpCliente())
                .userAgent(e.getUserAgent())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .version(e.getVersion())
                .build();
    }
}
