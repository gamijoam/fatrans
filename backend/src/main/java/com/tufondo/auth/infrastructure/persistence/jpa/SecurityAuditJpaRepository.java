package com.tufondo.auth.infrastructure.persistence.jpa;

import com.tufondo.auth.infrastructure.persistence.entity.SecurityAuditEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface SecurityAuditJpaRepository extends JpaRepository<SecurityAuditEntity, UUID> {

    Page<SecurityAuditEntity> findAllByOrderByTimestampDesc(Pageable pageable);

    Page<SecurityAuditEntity> findByUsuarioIdOrderByTimestampDesc(UUID usuarioId, Pageable pageable);

    Page<SecurityAuditEntity> findByTipoEventoOrderByTimestampDesc(String tipoEvento, Pageable pageable);

    Page<SecurityAuditEntity> findByTimestampBetweenOrderByTimestampDesc(
            Instant fechaInicio, Instant fechaFin, Pageable pageable);

    @Query("SELECT s FROM SecurityAuditEntity s WHERE " +
           "(:usuarioId IS NULL OR s.usuarioId = :usuarioId) AND " +
           "(:tipoEvento IS NULL OR s.tipoEvento = :tipoEvento) AND " +
           "(:fechaInicio IS NULL OR s.timestamp >= :fechaInicio) AND " +
           "(:fechaFin IS NULL OR s.timestamp <= :fechaFin) " +
           "ORDER BY s.timestamp DESC")
    Page<SecurityAuditEntity> buscarConFiltros(
            @Param("usuarioId") UUID usuarioId,
            @Param("tipoEvento") String tipoEvento,
            @Param("fechaInicio") Instant fechaInicio,
            @Param("fechaFin") Instant fechaFin,
            Pageable pageable);

    List<SecurityAuditEntity> findTop100ByOrderByTimestampDesc();

    List<SecurityAuditEntity> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, String entityId);
}