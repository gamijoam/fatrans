// com/tufondo/ahorros/infrastructure/persistence/jpa/MovimientoJpaRepository.java
package com.tufondo.ahorros.infrastructure.persistence.jpa;

import com.tufondo.ahorros.domain.model.enums.EstadoMovimiento;
import com.tufondo.ahorros.domain.model.enums.TipoMovimiento;
import com.tufondo.ahorros.infrastructure.persistence.entity.MovimientoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository para MovimientoEntity.
 */
@Repository
public interface MovimientoJpaRepository extends JpaRepository<MovimientoEntity, UUID> {
    
    Optional<MovimientoEntity> findByNumeroOperacion(String numeroOperacion);
    
    Page<MovimientoEntity> findByCuentaAhorroIdOrderByFechaMovimientoDesc(UUID cuentaAhorroId, Pageable pageable);
    
    @Query("SELECT m FROM MovimientoEntity m WHERE m.cuentaAhorroId = :cuentaId " +
           "AND m.fechaMovimiento BETWEEN :fechaInicio AND :fechaFin " +
           "ORDER BY m.fechaMovimiento DESC")
    Page<MovimientoEntity> findByCuentaYRangoFechas(
            @Param("cuentaId") UUID cuentaAhorroId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            Pageable pageable);
    
    Page<MovimientoEntity> findByCuentaAhorroIdAndTipoOrderByFechaMovimientoDesc(
            UUID cuentaAhorroId, TipoMovimiento tipo, Pageable pageable);
    
    Page<MovimientoEntity> findBySocioIdOrderByFechaMovimientoDesc(UUID socioId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(m.monto), 0) FROM MovimientoEntity m " +
           "WHERE m.socioId = :socioId AND m.tipo = :tipo " +
           "AND m.fechaMovimiento >= :fechaInicio AND m.estado = :estado")
    BigDecimal sumMontoBySocioIdAndTipoAndFechaMovimientoAfterAndEstado(
            @Param("socioId") UUID socioId,
            @Param("tipo") TipoMovimiento tipo,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("estado") EstadoMovimiento estado);
    
    long countByCuentaAhorroIdAndEstado(UUID cuentaAhorroId, EstadoMovimiento estado);
    
    boolean existsByNumeroOperacion(String numeroOperacion);

    @Query("SELECT COALESCE(SUM(m.monto), 0) FROM MovimientoEntity m WHERE m.tipo = 'DEPOSITO' AND m.fechaMovimiento >= :inicioMes")
    BigDecimal sumDepositosMes(@Param("inicioMes") LocalDateTime inicioMes);

    @Query("SELECT COALESCE(SUM(m.monto), 0) FROM MovimientoEntity m WHERE m.tipo = 'RETIRO' AND m.fechaMovimiento >= :inicioMes")
    BigDecimal sumRetirosMes(@Param("inicioMes") LocalDateTime inicioMes);

    @Query("SELECT COUNT(m) FROM MovimientoEntity m WHERE m.tipo = :tipo AND m.fechaMovimiento >= :fecha")
    long countByTipoAndFechaAfter(@Param("tipo") TipoMovimiento tipo, @Param("fecha") LocalDateTime fecha);
}