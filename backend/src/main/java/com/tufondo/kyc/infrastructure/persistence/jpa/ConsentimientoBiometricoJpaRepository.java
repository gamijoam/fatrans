package com.tufondo.kyc.infrastructure.persistence.jpa;

import com.tufondo.kyc.infrastructure.persistence.entity.ConsentimientoBiometricoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConsentimientoBiometricoJpaRepository extends JpaRepository<ConsentimientoBiometricoEntity, UUID> {

    @Query("SELECT c FROM ConsentimientoBiometricoEntity c " +
           "WHERE c.socioId = :socioId AND c.aceptado = true AND c.fechaRevocacion IS NULL " +
           "ORDER BY c.fechaConsentimiento DESC")
    Optional<ConsentimientoBiometricoEntity> findVigenteBySocioId(@Param("socioId") UUID socioId);
}
