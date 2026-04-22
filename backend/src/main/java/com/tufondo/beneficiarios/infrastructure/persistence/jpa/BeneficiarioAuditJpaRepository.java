// com/tufondo/beneficiarios/infrastructure/persistence/jpa/BeneficiarioAuditJpaRepository.java
package com.tufondo.beneficiarios.infrastructure.persistence.jpa;

import com.tufondo.beneficiarios.infrastructure.persistence.entity.BeneficiarioAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BeneficiarioAuditJpaRepository extends JpaRepository<BeneficiarioAuditEntity, UUID> {

    /**
     * Lista entradas de auditoría por tipo de entidad y ID de entidad.
     */
    List<BeneficiarioAuditEntity> findByEntidadTipoAndEntidadIdOrderByFechaEventoDesc(
            String entidadTipo, UUID entidadId);

    /**
     * Lista entradas de auditoría por usuario.
     */
    List<BeneficiarioAuditEntity> findByUsuarioIdOrderByFechaEventoDesc(String usuarioId);
}