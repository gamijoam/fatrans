// com/tufondo/creditos/infrastructure/persistence/adapter/AmortizacionRepositoryImpl.java
package com.tufondo.creditos.infrastructure.persistence.adapter;

import com.tufondo.creditos.domain.model.Amortizacion;
import com.tufondo.creditos.domain.model.enums.EstadoAmortizacion;
import com.tufondo.creditos.domain.repository.AmortizacionRepository;
import com.tufondo.creditos.infrastructure.persistence.entity.AmortizacionEntity;
import com.tufondo.creditos.infrastructure.persistence.jpa.AmortizacionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación del repositorio de Amortizacion.
 */
@Repository
@RequiredArgsConstructor
public class AmortizacionRepositoryImpl implements AmortizacionRepository {

    private final AmortizacionJpaRepository jpaRepository;

    @Override
    public Amortizacion guardar(Amortizacion amortizacion) {
        AmortizacionEntity entity = toEntity(amortizacion);
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }
        entity.setUpdatedAt(LocalDateTime.now());
        entity = jpaRepository.save(entity);
        return toDomain(entity);
    }

    @Override
    public List<Amortizacion> guardarBatch(List<Amortizacion> amortizaciones) {
        List<AmortizacionEntity> entities = amortizaciones.stream()
            .map(this::toEntity)
            .collect(Collectors.toList());
        
        // Set createdAt for entities without it
        LocalDateTime now = LocalDateTime.now();
        entities.forEach(e -> {
            if (e.getCreatedAt() == null) {
                e.setCreatedAt(now);
            }
            e.setUpdatedAt(now);
        });
        
        entities = jpaRepository.saveAll(entities);
        return entities.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<Amortizacion> buscarPorId(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Amortizacion> buscarPorIdWithLock(UUID id) {
        return jpaRepository.findByIdWithLock(id).map(this::toDomain);
    }

    @Override
    public List<Amortizacion> listarPorPlanId(UUID planId) {
        return jpaRepository.findByPlanId(planId).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Amortizacion> listarPorEstado(EstadoAmortizacion estado) {
        return jpaRepository.findByEstado(estado).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<Amortizacion> buscarPorReferenciaPago(String referenciaPago) {
        return jpaRepository.findByReferenciaPago(referenciaPago).map(this::toDomain);
    }

    @Override
    public boolean existePorReferenciaPago(String referenciaPago) {
        return jpaRepository.existsByReferenciaPago(referenciaPago);
    }

    @Override
    public long countByEstado(EstadoAmortizacion estado) {
        return jpaRepository.countByEstado(estado);
    }

    @Override
    public BigDecimal sumInteresesMoraPendientes() {
        return jpaRepository.sumInteresesMoraPendientes();
    }

    private Amortizacion toDomain(AmortizacionEntity entity) {
        return Amortizacion.builder()
            .id(entity.getId())
            .planId(entity.getPlanId())
            .numeroCuota(entity.getNumeroCuota())
            .fechaVencimiento(entity.getFechaVencimiento())
            .fechaPago(entity.getFechaPago())
            .capital(entity.getCapital())
            .interes(entity.getInteres())
            .seguros(entity.getSeguros())
            .comisiones(entity.getComisiones())
            .montoCuota(entity.getMontoCuota())
            .saldoInsoluto(entity.getSaldoInsoluto())
            .estado(entity.getEstado())
            .diasMora(entity.getDiasMora())
            .interesMora(entity.getInteresMora())
            .montoPagado(entity.getMontoPagado())
            .referenciaPago(entity.getReferenciaPago())
            .colateralEjecutada(entity.getColateralEjecutada())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .version(entity.getVersion())
            .build();
    }

    private AmortizacionEntity toEntity(Amortizacion domain) {
        return AmortizacionEntity.builder()
            .id(domain.getId())
            .planId(domain.getPlanId())
            .numeroCuota(domain.getNumeroCuota())
            .fechaVencimiento(domain.getFechaVencimiento())
            .fechaPago(domain.getFechaPago())
            .capital(domain.getCapital())
            .interes(domain.getInteres())
            .seguros(domain.getSeguros())
            .comisiones(domain.getComisiones())
            .montoCuota(domain.getMontoCuota())
            .saldoInsoluto(domain.getSaldoInsoluto())
            .estado(domain.getEstado())
            .diasMora(domain.getDiasMora())
            .interesMora(domain.getInteresMora())
            .montoPagado(domain.getMontoPagado())
            .referenciaPago(domain.getReferenciaPago())
            .colateralEjecutada(domain.getColateralEjecutada())
            .createdAt(domain.getCreatedAt())
            .updatedAt(domain.getUpdatedAt())
            .version(domain.getVersion())
            .build();
    }
}
