package com.tufondo.contabilidad.infrastructure.persistence.jpa;

import com.tufondo.contabilidad.domain.model.enums.EstadoAsiento;
import com.tufondo.contabilidad.domain.model.enums.OrigenAsiento;
import com.tufondo.contabilidad.infrastructure.persistence.entity.AsientoContableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AsientoContableJpaRepository extends JpaRepository<AsientoContableEntity, UUID> {

    Optional<AsientoContableEntity> findByNumero(Long numero);

    List<AsientoContableEntity> findByFechaContableBetweenOrderByNumeroAsc(
            LocalDate desde, LocalDate hasta);

    List<AsientoContableEntity> findByOrigenAndFechaContableBetweenOrderByFechaContableAscNumeroAsc(
            OrigenAsiento origen, LocalDate desde, LocalDate hasta);

    List<AsientoContableEntity> findByEstadoOrderByNumeroAsc(EstadoAsiento estado);

    List<AsientoContableEntity> findByReferenciaExternaOrderByNumeroAsc(String referenciaExterna);

    List<AsientoContableEntity> findByAsientoReversaIdOrderByNumeroAsc(UUID asientoReversaId);

    /**
     * Obtiene el próximo valor de la secuencia {@code seq_asiento_numero}.
     *
     * <p>El correlativo lo asigna BD para garantizar continuidad bajo
     * concurrencia (dos requests simultáneos obtienen números distintos).
     * Si lo asignáramos en Java tendríamos race conditions y huecos en
     * el correlativo — los libros legales SUDECA exigen secuencia
     * ininterrumpida.</p>
     */
    @Query(value = "SELECT nextval('seq_asiento_numero')", nativeQuery = true)
    Long siguienteNumeroAsiento();
}
