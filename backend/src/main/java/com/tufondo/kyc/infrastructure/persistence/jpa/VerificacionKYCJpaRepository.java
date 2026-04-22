// com.tufondo.kyc.infrastructure.persistence.jpa.VerificacionKYCJpaRepository
package com.tufondo.kyc.infrastructure.persistence.jpa;

import com.tufondo.kyc.domain.model.enums.EstadoVerificacion;
import com.tufondo.kyc.infrastructure.persistence.entity.VerificacionKYCEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificacionKYCJpaRepository extends JpaRepository<VerificacionKYCEntity, UUID> {

    Optional<VerificacionKYCEntity> findBySocioId(UUID socioId);

    Optional<VerificacionKYCEntity> findFirstBySocioIdOrderByFechaInicioDesc(UUID socioId);

    List<VerificacionKYCEntity> findByEstado(EstadoVerificacion estado);

    List<VerificacionKYCEntity> findByEstadoIn(List<EstadoVerificacion> estados);

    List<VerificacionKYCEntity> findAllByOrderByFechaInicioDesc();

    List<VerificacionKYCEntity> findByEstadoOrderByFechaInicioAsc(EstadoVerificacion estado);

    Long countByEstado(EstadoVerificacion estado);

    boolean existsBySocioIdAndEstadoIn(UUID socioId, List<EstadoVerificacion> estados);

    @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, v.fechaInicio, v.fechaRevision)) " +
           "FROM VerificacionKYCEntity v " +
           "WHERE v.estado = 'APROBADO' AND v.fechaRevision IS NOT NULL")
    Double calculateTiempoPromedioRevisionHoras();

    @Query("SELECT COUNT(v) FROM VerificacionKYCEntity v " +
           "WHERE v.estado = 'APROBADO' " +
           "AND v.fechaExpiracion BETWEEN :now AND :futureDate")
    Long countPorExpirarEntreFechas(@Param("now") LocalDateTime now, @Param("futureDate") LocalDateTime futureDate);
}