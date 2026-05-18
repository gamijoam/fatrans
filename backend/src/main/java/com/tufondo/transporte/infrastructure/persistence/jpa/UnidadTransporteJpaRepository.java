package com.tufondo.transporte.infrastructure.persistence.jpa;

import com.tufondo.transporte.infrastructure.persistence.entity.UnidadTransporteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UnidadTransporteJpaRepository extends JpaRepository<UnidadTransporteEntity, UUID> {
    List<UnidadTransporteEntity> findBySocioId(UUID socioId);
}
