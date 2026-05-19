package com.tufondo.kyc.infrastructure.persistence.jpa;

import com.tufondo.kyc.infrastructure.persistence.entity.VerificacionBiometricaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificacionBiometricaJpaRepository extends JpaRepository<VerificacionBiometricaEntity, UUID> {

    Optional<VerificacionBiometricaEntity> findByProveedorAndProveedorSessionId(String proveedor, String sessionId);

    List<VerificacionBiometricaEntity> findByVerificacionKycId(UUID verificacionKycId);

    List<VerificacionBiometricaEntity> findBySocioId(UUID socioId);

    Optional<VerificacionBiometricaEntity> findFirstBySocioIdOrderByFechaInicioDesc(UUID socioId);

    void deleteAllBySocioId(UUID socioId);
}
