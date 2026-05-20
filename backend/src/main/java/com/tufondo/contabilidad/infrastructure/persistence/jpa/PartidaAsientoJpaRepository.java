package com.tufondo.contabilidad.infrastructure.persistence.jpa;

import com.tufondo.contabilidad.infrastructure.persistence.entity.PartidaAsientoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface PartidaAsientoJpaRepository extends JpaRepository<PartidaAsientoEntity, UUID> {

    List<PartidaAsientoEntity> findByAsientoIdOrderByOrdenAsc(UUID asientoId);

    /**
     * Carga todas las partidas de varios asientos en UNA sola query (evita
     * N+1 cuando se listan muchos asientos para el Libro Diario).
     */
    List<PartidaAsientoEntity> findByAsientoIdInOrderByAsientoIdAscOrdenAsc(
            Collection<UUID> asientoIds);
}
