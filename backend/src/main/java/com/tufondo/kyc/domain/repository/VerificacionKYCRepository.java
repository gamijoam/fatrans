// com.tufondo.kyc.domain.repository.VerificacionKYCRepository
package com.tufondo.kyc.domain.repository;

import com.tufondo.kyc.domain.model.VerificacionKYC;
import com.tufondo.kyc.domain.model.enums.EstadoVerificacion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface para VerificacionKYC.
 */
public interface VerificacionKYCRepository {

    Optional<VerificacionKYC> findById(UUID id);

    Optional<VerificacionKYC> findBySocioId(UUID socioId);

    Optional<VerificacionKYC> findActiveBySocioId(UUID socioId);

    List<VerificacionKYC> findByEstado(EstadoVerificacion estado);

    List<VerificacionKYC> findByEstadoIn(List<EstadoVerificacion> estados);

    List<VerificacionKYC> findAllOrderByFechaInicioDesc();

    List<VerificacionKYC> findByRevisionPendienteOrderByFechaAsc();

    Long countByEstado(EstadoVerificacion estado);

    Long countBySocioIdAndEstadoIn(UUID socioId, List<EstadoVerificacion> estados);

    boolean existsBySocioIdAndEstadoIn(UUID socioId, List<EstadoVerificacion> estados);

    VerificacionKYC save(VerificacionKYC verificacion);

    void delete(UUID id);

    Double calculateTiempoPromedioRevisionHoras();

    Long countPorExpirarEntreFechas(LocalDateTime now, LocalDateTime futureDate);
}