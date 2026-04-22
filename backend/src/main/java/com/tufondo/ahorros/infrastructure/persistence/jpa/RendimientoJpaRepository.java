// com/tufondo/ahorros/infrastructure/persistence/jpa/RendimientoJpaRepository.java
package com.tufondo.ahorros.infrastructure.persistence.jpa;

import com.tufondo.ahorros.domain.model.enums.EstadoAplicacion;
import com.tufondo.ahorros.domain.model.enums.TipoRendimiento;
import com.tufondo.ahorros.infrastructure.persistence.entity.RendimientoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository para RendimientoEntity.
 */
@Repository
public interface RendimientoJpaRepository extends JpaRepository<RendimientoEntity, UUID> {
    
    Page<RendimientoEntity> findByCuentaAhorroIdOrderByFechaCalculoDesc(UUID cuentaAhorroId, Pageable pageable);
    
    @Query("SELECT r FROM RendimientoEntity r WHERE r.cuentaAhorroId = :cuentaId " +
           "AND r.periodoInicio = :periodoInicio AND r.periodoFin = :periodoFin")
    Optional<RendimientoEntity> findByCuentaAndPeriodo(
            @Param("cuentaId") UUID cuentaAhorroId,
            @Param("periodoInicio") LocalDate periodoInicio,
            @Param("periodoFin") LocalDate periodoFin);
    
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM RendimientoEntity r " +
           "WHERE r.cuentaAhorroId = :cuentaId AND r.periodoInicio = :periodoInicio " +
           "AND r.periodoFin = :periodoFin AND r.tipo = :tipo")
    boolean existsByCuentaAndPeriodoAndTipo(
            @Param("cuentaId") UUID cuentaAhorroId,
            @Param("periodoInicio") LocalDate periodoInicio,
            @Param("periodoFin") LocalDate periodoFin,
            @Param("tipo") TipoRendimiento tipo);
    
    Page<RendimientoEntity> findByEstadoAplicacionOrderByFechaCalculoDesc(
            EstadoAplicacion estado, Pageable pageable);
    
    @Query("SELECT r FROM RendimientoEntity r WHERE r.fechaCalculo BETWEEN :fechaInicio AND :fechaFin")
    Page<RendimientoEntity> findByRangoFechasCalculo(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin,
            Pageable pageable);
}