package com.tufondo.compliance.infrastructure.persistence.jpa;

import com.tufondo.compliance.infrastructure.persistence.entity.ConsentimientoLocdoftEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ConsentimientoLocdoftJpaRepository extends JpaRepository<ConsentimientoLocdoftEntity, UUID> {

    @Modifying
    @Query("UPDATE ConsentimientoLocdoftEntity c SET c.movimientoId = :movimientoId WHERE c.id = :id")
    int actualizarMovimientoId(@Param("id") UUID id, @Param("movimientoId") UUID movimientoId);
}
