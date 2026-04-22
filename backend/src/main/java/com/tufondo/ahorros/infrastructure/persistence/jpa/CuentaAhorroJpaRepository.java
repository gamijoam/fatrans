// com/tufondo/ahorros/infrastructure/persistence/jpa/CuentaAhorroJpaRepository.java
package com.tufondo.ahorros.infrastructure.persistence.jpa;

import com.tufondo.ahorros.domain.model.enums.EstadoCuenta;
import com.tufondo.ahorros.domain.model.enums.TipoCuenta;
import com.tufondo.ahorros.infrastructure.persistence.entity.CuentaAhorroEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository para CuentaAhorroEntity.
 */
@Repository
public interface CuentaAhorroJpaRepository extends JpaRepository<CuentaAhorroEntity, UUID> {
    
    Optional<CuentaAhorroEntity> findByNumeroCuenta(String numeroCuenta);
    
    Page<CuentaAhorroEntity> findBySocioId(UUID socioId, Pageable pageable);

    Page<CuentaAhorroEntity> findByEstado(EstadoCuenta estado, Pageable pageable);

    boolean existsBySocioIdAndTipoCuenta(UUID socioId, TipoCuenta tipoCuenta);
    
    @Query("SELECT c FROM CuentaAhorroEntity c WHERE c.estado = :estado")
    Page<CuentaAhorroEntity> findCuentasActivas(@Param("estado") EstadoCuenta estado, Pageable pageable);
    
    boolean existsByNumeroCuenta(String numeroCuenta);

    long count();

    long countByEstado(EstadoCuenta estado);

    @Query("SELECT COALESCE(SUM(c.saldoActual), 0) FROM CuentaAhorroEntity c WHERE c.estado = 'ACTIVA'")
    java.math.BigDecimal sumSaldoActualCuentasActivas();
}