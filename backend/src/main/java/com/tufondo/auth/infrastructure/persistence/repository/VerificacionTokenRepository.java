package com.tufondo.auth.infrastructure.persistence.repository;

import com.tufondo.auth.infrastructure.persistence.entity.VerificacionTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificacionTokenRepository extends JpaRepository<VerificacionTokenEntity, UUID> {

    Optional<VerificacionTokenEntity> findByToken(String token);

    Optional<VerificacionTokenEntity> findByTokenAndUsedFalse(String token);

    Optional<VerificacionTokenEntity> findByUsuarioIdAndTipoAndUsedFalseAndExpiresAtAfter(
            UUID usuarioId,
            com.tufondo.socios.domain.model.enums.TipoVerificacion tipo,
            Instant now
    );

    void deleteByExpiresAtBefore(Instant now);
}