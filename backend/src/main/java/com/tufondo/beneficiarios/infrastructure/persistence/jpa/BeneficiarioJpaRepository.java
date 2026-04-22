// com/tufondo/beneficiarios/infrastructure/persistence/jpa/BeneficiarioJpaRepository.java
package com.tufondo.beneficiarios.infrastructure.persistence.jpa;

import com.tufondo.beneficiarios.infrastructure.persistence.entity.BeneficiarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BeneficiarioJpaRepository extends JpaRepository<BeneficiarioEntity, UUID> {

    /**
     * Lista todos los beneficiarios activos de un socio.
     */
    List<BeneficiarioEntity> findBySocioIdAndActivoTrue(UUID socioId);

    /**
     * Cuenta los beneficiarios activos de un socio.
     */
    int countBySocioIdAndActivoTrue(UUID socioId);

    /**
     * Verifica si existe un beneficiario activo con el mismo documento.
     */
    @Query("SELECT COUNT(b) > 0 FROM BeneficiarioEntity b " +
           "WHERE b.socioId = :socioId AND b.tipoDocumento = :tipoDocumento " +
           "AND b.numeroDocumento = :numeroDocumento AND b.activo = true " +
           "AND (:excludeId IS NULL OR b.id != :excludeId)")
    boolean existePorDocumento(@Param("socioId") UUID socioId,
                               @Param("tipoDocumento") com.tufondo.beneficiarios.domain.model.enums.TipoDocumento tipoDocumento,
                               @Param("numeroDocumento") String numeroDocumento,
                               @Param("excludeId") UUID excludeId);

    /**
     * Suma los porcentajes de beneficiarios activos de un socio.
     */
    @Query("SELECT COALESCE(SUM(b.porcentaje), 0) FROM BeneficiarioEntity b " +
           "WHERE b.socioId = :socioId AND b.activo = true")
    BigDecimal sumarPorcentajesPorSocioId(@Param("socioId") UUID socioId);

    /**
     * Busca un beneficiario por ID sin filtrar por estado activo.
     */
    Optional<BeneficiarioEntity> findById(UUID id);
}