// com/tufondo/creditos/infrastructure/persistence/adapter/SolicitudCreditoRepositoryImpl.java
package com.tufondo.creditos.infrastructure.persistence.adapter;

import com.tufondo.creditos.domain.model.SolicitudCredito;
import com.tufondo.creditos.domain.model.enums.EstadoSolicitud;
import com.tufondo.creditos.domain.repository.SolicitudCreditoRepository;
import com.tufondo.creditos.infrastructure.persistence.entity.SolicitudCreditoEntity;
import com.tufondo.creditos.infrastructure.persistence.jpa.SolicitudCreditoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación del repositorio de SolicitudCredito.
 */
@Repository
@RequiredArgsConstructor
public class SolicitudCreditoRepositoryImpl implements SolicitudCreditoRepository {

    private final SolicitudCreditoJpaRepository jpaRepository;

    @Override
    public SolicitudCredito guardar(SolicitudCredito solicitud) {
        SolicitudCreditoEntity entity = toEntity(solicitud);
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }
        entity.setUpdatedAt(LocalDateTime.now());
        entity = jpaRepository.save(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<SolicitudCredito> buscarPorId(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<SolicitudCredito> buscarPorNumeroSolicitud(String numeroSolicitud) {
        return jpaRepository.findByNumeroSolicitud(numeroSolicitud).map(this::toDomain);
    }

    @Override
    public List<SolicitudCredito> listarPorSocioId(UUID socioId) {
        return jpaRepository.findBySocioId(socioId).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<SolicitudCredito> listarPorEstado(EstadoSolicitud estado) {
        return jpaRepository.findByEstado(estado).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public boolean existeCreditoActivoPorSocio(UUID socioId) {
        return jpaRepository.existsBySocioIdAndEstado(socioId, EstadoSolicitud.DESEMBOLSADO);
    }

    @Override
    public List<SolicitudCredito> listarTodos() {
        return jpaRepository.findAll().stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public boolean existePorNumeroSolicitud(String numeroSolicitud) {
        return jpaRepository.existsByNumeroSolicitud(numeroSolicitud);
    }

    @Override
    public long countByEstado(EstadoSolicitud estado) {
        return jpaRepository.countByEstado(estado);
    }

    @Override
    public long countByEstadoAndCreatedAtAfter(EstadoSolicitud estado, LocalDateTime fecha) {
        return jpaRepository.countByEstadoAndCreatedAtAfter(estado, fecha);
    }

    @Override
    public BigDecimal sumMontoSolicitadoByEstado(EstadoSolicitud estado) {
        return jpaRepository.sumMontoSolicitadoByEstado(estado);
    }

    private SolicitudCredito toDomain(SolicitudCreditoEntity entity) {
        return SolicitudCredito.builder()
            .id(entity.getId())
            .numeroSolicitud(entity.getNumeroSolicitud())
            .socioId(entity.getSocioId())
            .tipoCreditoId(entity.getTipoCreditoId())
            .montoSolicitado(entity.getMontoSolicitado())
            .plazoMeses(entity.getPlazoMeses())
            .tasaInteresAplicada(entity.getTasaInteresAplicada())
            .cuotaMensualEstimada(entity.getCuotaMensualEstimada())
            .estado(entity.getEstado())
            .colateralCuentaId(entity.getColateralCuentaId())
            .colateralMontoRetenido(entity.getColateralMontoRetenido())
            .destinoCredito(entity.getDestinoCredito())
            .evaluacionId(entity.getEvaluacionId())
            .planAmortizacionId(entity.getPlanAmortizacionId())
            .referenciaDesembolso(entity.getReferenciaDesembolso())
            .cuentaDestino(entity.getCuentaDestino())
            .notas(entity.getNotas())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .version(entity.getVersion())
            .build();
    }

    private SolicitudCreditoEntity toEntity(SolicitudCredito domain) {
        SolicitudCreditoEntity.SolicitudCreditoEntityBuilder builder = SolicitudCreditoEntity.builder()
            .numeroSolicitud(domain.getNumeroSolicitud())
            .socioId(domain.getSocioId())
            .tipoCreditoId(domain.getTipoCreditoId())
            .montoSolicitado(domain.getMontoSolicitado())
            .plazoMeses(domain.getPlazoMeses())
            .tasaInteresAplicada(domain.getTasaInteresAplicada())
            .cuotaMensualEstimada(domain.getCuotaMensualEstimada())
            .estado(domain.getEstado())
            .colateralCuentaId(domain.getColateralCuentaId())
            .colateralMontoRetenido(domain.getColateralMontoRetenido())
            .destinoCredito(domain.getDestinoCredito())
            .evaluacionId(domain.getEvaluacionId())
            .planAmortizacionId(domain.getPlanAmortizacionId())
            .referenciaDesembolso(domain.getReferenciaDesembolso())
            .cuentaDestino(domain.getCuentaDestino())
            .notas(domain.getNotas())
            .createdAt(domain.getCreatedAt())
            .updatedAt(domain.getUpdatedAt());

        if (domain.getId() != null) {
            builder.id(domain.getId());
        }

        return builder.build();
    }
}
