// com.tufondo.kyc.infrastructure.persistence.jpa.AuditKYCJpaRepository
package com.tufondo.kyc.infrastructure.persistence.jpa;

import com.tufondo.kyc.infrastructure.persistence.entity.AuditKYCEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository JPA para auditoría KYC.
 */
@Repository
public interface AuditKYCJpaRepository extends JpaRepository<AuditKYCEntity, UUID> {

    /**
     * Buscar eventos de auditoría por socio.
     */
    List<AuditKYCEntity> findBySocioIdOrderByFechaEventoDesc(UUID socioId);

    /**
     * Buscar eventos por tipo de evento.
     */
    List<AuditKYCEntity> findByTipoEventoOrderByFechaEventoDesc(AuditKYCEntity.TipoEventoAuditoria tipoEvento);

    /**
     * Buscar eventos por usuario.
     */
    List<AuditKYCEntity> findByUsuarioIdOrderByFechaEventoDesc(String usuarioId);

    /**
     * Buscar eventos en un rango de fechas.
     */
    List<AuditKYCEntity> findByFechaEventoBetweenOrderByFechaEventoDesc(
        LocalDateTime inicio, LocalDateTime fin);

    /**
     * Buscar eventos fallidos para un socio.
     */
    List<AuditKYCEntity> findBySocioIdAndExitosoFalseOrderByFechaEventoDesc(UUID socioId);

    /**
     * Buscar eventos de acceso no autorizado.
     */
    List<AuditKYCEntity> findByTipoEventoInOrderByFechaEventoDesc(
        List<AuditKYCEntity.TipoEventoAuditoria> tipos);
}