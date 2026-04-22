// com.tufondo.kyc.infrastructure.persistence.adapter.VerificacionKYCRepositoryImpl
package com.tufondo.kyc.infrastructure.persistence.adapter;

import com.tufondo.kyc.domain.model.VerificacionKYC;
import com.tufondo.kyc.domain.model.enums.EstadoVerificacion;
import com.tufondo.kyc.domain.repository.VerificacionKYCRepository;
import com.tufondo.kyc.infrastructure.persistence.entity.VerificacionKYCEntity;
import com.tufondo.kyc.infrastructure.persistence.jpa.VerificacionKYCJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class VerificacionKYCRepositoryImpl implements VerificacionKYCRepository {

    private final VerificacionKYCJpaRepository jpaRepository;

    @Override
    public Optional<VerificacionKYC> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<VerificacionKYC> findBySocioId(UUID socioId) {
        return jpaRepository.findBySocioId(socioId).map(this::toDomain);
    }

    @Override
    public Optional<VerificacionKYC> findActiveBySocioId(UUID socioId) {
        List<EstadoVerificacion> estadosActivos = List.of(
            EstadoVerificacion.PENDIENTE,
            EstadoVerificacion.EN_REVISION,
            EstadoVerificacion.APROBADO
        );
        return jpaRepository.findFirstBySocioIdOrderByFechaInicioDesc(socioId)
            .filter(e -> estadosActivos.contains(e.getEstado()))
            .map(this::toDomain);
    }

    @Override
    public List<VerificacionKYC> findByEstado(EstadoVerificacion estado) {
        return jpaRepository.findByEstado(estado).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<VerificacionKYC> findByEstadoIn(List<EstadoVerificacion> estados) {
        return jpaRepository.findByEstadoIn(estados).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<VerificacionKYC> findAllOrderByFechaInicioDesc() {
        return jpaRepository.findAllByOrderByFechaInicioDesc().stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<VerificacionKYC> findByRevisionPendienteOrderByFechaAsc() {
        return jpaRepository.findByEstadoOrderByFechaInicioAsc(EstadoVerificacion.EN_REVISION)
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Long countByEstado(EstadoVerificacion estado) {
        return jpaRepository.countByEstado(estado);
    }

    @Override
    public Long countBySocioIdAndEstadoIn(UUID socioId, List<EstadoVerificacion> estados) {
        return jpaRepository.existsBySocioIdAndEstadoIn(socioId, estados) ? 1L : 0L;
    }

    @Override
    public boolean existsBySocioIdAndEstadoIn(UUID socioId, List<EstadoVerificacion> estados) {
        return jpaRepository.existsBySocioIdAndEstadoIn(socioId, estados);
    }

    @Override
    public VerificacionKYC save(VerificacionKYC verificacion) {
        VerificacionKYCEntity entity = toEntity(verificacion);
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }
        entity.setUpdatedAt(LocalDateTime.now());
        entity = jpaRepository.save(entity);
        return toDomain(entity);
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public Double calculateTiempoPromedioRevisionHoras() {
        Double result = jpaRepository.calculateTiempoPromedioRevisionHoras();
        return result != null ? result : 0.0;
    }

    @Override
    public Long countPorExpirarEntreFechas(LocalDateTime now, LocalDateTime futureDate) {
        return jpaRepository.countPorExpirarEntreFechas(now, futureDate);
    }

    private VerificacionKYC toDomain(VerificacionKYCEntity entity) {
        return VerificacionKYC.builder()
            .id(entity.getId())
            .socioId(entity.getSocioId())
            .nivel(entity.getNivel())
            .estado(entity.getEstado())
            .fechaInicio(entity.getFechaInicio())
            .fechaCompletado(entity.getFechaCompletado())
            .fechaExpiracion(entity.getFechaExpiracion())
            .datosVerificacionAutomatica(entity.getDatosVerificacionAutomatica())
            .revisadoPor(entity.getRevisadoPor())
            .fechaRevision(entity.getFechaRevision())
            .comentariosRevision(entity.getComentariosRevision())
            .motivoRechazo(entity.getMotivoRechazo())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    private VerificacionKYCEntity toEntity(VerificacionKYC domain) {
        return VerificacionKYCEntity.builder()
            .id(domain.getId())
            .socioId(domain.getSocioId())
            .nivel(domain.getNivel())
            .estado(domain.getEstado())
            .fechaInicio(domain.getFechaInicio())
            .fechaCompletado(domain.getFechaCompletado())
            .fechaExpiracion(domain.getFechaExpiracion())
            .datosVerificacionAutomatica(domain.getDatosVerificacionAutomatica())
            .revisadoPor(domain.getRevisadoPor())
            .fechaRevision(domain.getFechaRevision())
            .comentariosRevision(domain.getComentariosRevision())
            .motivoRechazo(domain.getMotivoRechazo())
            .createdAt(domain.getCreatedAt())
            .updatedAt(domain.getUpdatedAt())
            .build();
    }
}