// com/tufondo/creditos/infrastructure/persistence/adapter/PlanAmortizacionRepositoryImpl.java
package com.tufondo.creditos.infrastructure.persistence.adapter;

import com.tufondo.creditos.domain.model.PlanAmortizacion;
import com.tufondo.creditos.domain.repository.PlanAmortizacionRepository;
import com.tufondo.creditos.infrastructure.persistence.entity.PlanAmortizacionEntity;
import com.tufondo.creditos.infrastructure.persistence.jpa.PlanAmortizacionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementación del repositorio de PlanAmortizacion.
 */
@Repository
@RequiredArgsConstructor
public class PlanAmortizacionRepositoryImpl implements PlanAmortizacionRepository {

    private final PlanAmortizacionJpaRepository jpaRepository;

    @Override
    public PlanAmortizacion guardar(PlanAmortizacion plan) {
        PlanAmortizacionEntity entity = toEntity(plan);
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }
        entity.setUpdatedAt(LocalDateTime.now());
        entity = jpaRepository.save(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<PlanAmortizacion> buscarPorId(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<PlanAmortizacion> buscarPorSolicitudId(UUID solicitudId) {
        return jpaRepository.findBySolicitudId(solicitudId).map(this::toDomain);
    }

    @Override
    public boolean existePorSolicitudId(UUID solicitudId) {
        return jpaRepository.existsBySolicitudId(solicitudId);
    }

    private PlanAmortizacion toDomain(PlanAmortizacionEntity entity) {
        return PlanAmortizacion.builder()
            .id(entity.getId())
            .solicitudId(entity.getSolicitudId())
            .montoPrincipal(entity.getMontoPrincipal())
            .tasaInteres(entity.getTasaInteres())
            .plazoMeses(entity.getPlazoMeses())
            .frecuenciaPago(entity.getFrecuenciaPago())
            .fechaInicio(entity.getFechaInicio())
            .fechaFin(entity.getFechaFin())
            .totalIntereses(entity.getTotalIntereses())
            .totalPagado(entity.getTotalPagado())
            .saldoPendiente(entity.getSaldoPendiente())
            .numeroCuotas(entity.getNumeroCuotas())
            .cuotaMensual(entity.getCuotaMensual())
            .estado(entity.getEstado())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .version(entity.getVersion())
            .build();
    }

    private PlanAmortizacionEntity toEntity(PlanAmortizacion domain) {
        return PlanAmortizacionEntity.builder()
            .id(domain.getId())
            .solicitudId(domain.getSolicitudId())
            .montoPrincipal(domain.getMontoPrincipal())
            .tasaInteres(domain.getTasaInteres())
            .plazoMeses(domain.getPlazoMeses())
            .frecuenciaPago(domain.getFrecuenciaPago())
            .fechaInicio(domain.getFechaInicio())
            .fechaFin(domain.getFechaFin())
            .totalIntereses(domain.getTotalIntereses())
            .totalPagado(domain.getTotalPagado())
            .saldoPendiente(domain.getSaldoPendiente())
            .numeroCuotas(domain.getNumeroCuotas())
            .cuotaMensual(domain.getCuotaMensual())
            .estado(domain.getEstado())
            .createdAt(domain.getCreatedAt())
            .updatedAt(domain.getUpdatedAt())
            .version(domain.getVersion())
            .build();
    }
}
