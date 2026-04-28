package com.tufondo.tipocambio.infrastructure.persistence.jpa;

import com.tufondo.tipocambio.infrastructure.persistence.entity.TipoCambioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TipoCambioJpaRepository extends JpaRepository<TipoCambioEntity, UUID> {

    Optional<TipoCambioEntity> findByFecha(LocalDate fecha);

    boolean existsByFecha(LocalDate fecha);

    @Query("SELECT t FROM TipoCambioEntity t ORDER BY t.fecha DESC LIMIT 1")
    Optional<TipoCambioEntity> findTasaActual();

    @Query(value = "SELECT * FROM tipos_cambio ORDER BY fecha DESC LIMIT :limit", nativeQuery = true)
    java.util.List<TipoCambioEntity> findHistorial(@Param("limit") int limit);
}