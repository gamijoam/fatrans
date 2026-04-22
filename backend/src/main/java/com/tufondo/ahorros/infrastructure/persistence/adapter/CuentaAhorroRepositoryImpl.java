// com/tufondo/ahorros/infrastructure/persistence/adapter/CuentaAhorroRepositoryImpl.java
package com.tufondo.ahorros.infrastructure.persistence.adapter;

import com.tufondo.ahorros.domain.model.CuentaAhorro;
import com.tufondo.ahorros.domain.model.enums.EstadoCuenta;
import com.tufondo.ahorros.domain.model.enums.TipoCuenta;
import com.tufondo.ahorros.domain.repository.CuentaAhorroRepository;
import com.tufondo.ahorros.infrastructure.persistence.entity.CuentaAhorroEntity;
import com.tufondo.ahorros.infrastructure.persistence.jpa.CuentaAhorroJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementación del repositorio de CuentaAhorro.
 */
@Repository
@RequiredArgsConstructor
public class CuentaAhorroRepositoryImpl implements CuentaAhorroRepository {

    private final CuentaAhorroJpaRepository jpaRepository;

    @Override
    public CuentaAhorro guardar(CuentaAhorro cuenta) {
        CuentaAhorroEntity entity = toEntity(cuenta);
        entity = jpaRepository.save(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<CuentaAhorro> buscarPorId(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<CuentaAhorro> buscarPorNumeroCuenta(String numeroCuenta) {
        return jpaRepository.findByNumeroCuenta(numeroCuenta).map(this::toDomain);
    }

    @Override
    public Page<CuentaAhorro> buscarPorSocioId(UUID socioId, Pageable pageable) {
        return jpaRepository.findBySocioId(socioId, pageable).map(this::toDomain);
    }

    @Override
    public boolean existePorSocioIdYTipo(UUID socioId, TipoCuenta tipoCuenta) {
        return jpaRepository.existsBySocioIdAndTipoCuenta(socioId, tipoCuenta);
    }

    @Override
    public Page<CuentaAhorro> buscarPorEstado(EstadoCuenta estado, Pageable pageable) {
        return jpaRepository.findByEstado(estado, pageable).map(this::toDomain);
    }

    @Override
    public Page<CuentaAhorro> buscarCuentasActivas(Pageable pageable) {
        return jpaRepository.findCuentasActivas(EstadoCuenta.ACTIVA, pageable).map(this::toDomain);
    }
    
    @Override
    public boolean existePorNumeroCuenta(String numeroCuenta) {
        return jpaRepository.existsByNumeroCuenta(numeroCuenta);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public long countByEstado(EstadoCuenta estado) {
        return jpaRepository.countByEstado(estado);
    }

    @Override
    public java.math.BigDecimal sumSaldoActualCuentasActivas() {
        return jpaRepository.sumSaldoActualCuentasActivas();
    }

    private CuentaAhorro toDomain(CuentaAhorroEntity entity) {
        return CuentaAhorro.builder()
                .id(entity.getId())
                .numeroCuenta(entity.getNumeroCuenta())
                .socioId(entity.getSocioId())
                .saldoActual(entity.getSaldoActual())
                .saldoRetenido(entity.getSaldoRetenido())
                .tasaInteres(entity.getTasaInteres())
                .montoMinimoRequerido(entity.getMontoMinimoRequerido())
                .estado(entity.getEstado())
                .tipoCuenta(entity.getTipoCuenta())
                .fechaApertura(entity.getFechaApertura())
                .fechaUltimaOperacion(entity.getFechaUltimaOperacion())
                .version(entity.getVersion())
                .build();
    }

    private CuentaAhorroEntity toEntity(CuentaAhorro cuenta) {
        return CuentaAhorroEntity.builder()
                .id(cuenta.getId())
                .numeroCuenta(cuenta.getNumeroCuenta())
                .socioId(cuenta.getSocioId())
                .saldoActual(cuenta.getSaldoActual())
                .saldoRetenido(cuenta.getSaldoRetenido())
                .tasaInteres(cuenta.getTasaInteres())
                .montoMinimoRequerido(cuenta.getMontoMinimoRequerido())
                .estado(cuenta.getEstado())
                .tipoCuenta(cuenta.getTipoCuenta())
                .fechaApertura(cuenta.getFechaApertura())
                .fechaUltimaOperacion(cuenta.getFechaUltimaOperacion())
                .version(cuenta.getVersion())
                .build();
    }
}