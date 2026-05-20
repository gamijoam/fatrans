package com.tufondo.contabilidad.infrastructure.persistence.jpa;

import com.tufondo.contabilidad.domain.model.enums.TipoCuentaContable;
import com.tufondo.contabilidad.infrastructure.persistence.entity.CuentaContableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA del plan de cuentas. Definimos solo las queries derivadas
 * que el adapter de dominio necesita; nada más para evitar acoplamiento al
 * dataset entity desde fuera del módulo de infraestructura.
 */
@Repository
public interface CuentaContableJpaRepository extends JpaRepository<CuentaContableEntity, UUID> {

    Optional<CuentaContableEntity> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<CuentaContableEntity> findAllByOrderByCodigoAsc();

    List<CuentaContableEntity> findByTipoOrderByCodigoAsc(TipoCuentaContable tipo);

    /**
     * Hojas operativas activas (las únicas que pueden recibir movimientos).
     * Ordenadas por código para que el UI las muestre agrupadas naturalmente.
     */
    List<CuentaContableEntity> findByAceptaMovimientosTrueAndActivaTrueOrderByCodigoAsc();

    List<CuentaContableEntity> findByCuentaPadreIdOrderByCodigoAsc(UUID cuentaPadreId);
}
