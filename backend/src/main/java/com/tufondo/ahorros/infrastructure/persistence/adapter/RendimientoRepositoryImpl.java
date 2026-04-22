// com/tufondo/ahorros/infrastructure/persistence/adapter/RendimientoRepositoryImpl.java
package com.tufondo.ahorros.infrastructure.persistence.adapter;

import com.tufondo.ahorros.domain.model.Rendimiento;
import com.tufondo.ahorros.domain.model.enums.EstadoAplicacion;
import com.tufondo.ahorros.domain.model.enums.TipoRendimiento;
import com.tufondo.ahorros.domain.repository.RendimientoRepository;
import com.tufondo.ahorros.infrastructure.persistence.entity.RendimientoEntity;
import com.tufondo.ahorros.infrastructure.persistence.jpa.RendimientoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementación del repositorio de Rendimiento.
 */
@Repository
@RequiredArgsConstructor
public class RendimientoRepositoryImpl implements RendimientoRepository {

    private final RendimientoJpaRepository jpaRepository;

    @Override
    public Rendimiento guardar(Rendimiento rendimiento) {
        RendimientoEntity entity = toEntity(rendimiento);
        entity = jpaRepository.save(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<Rendimiento> buscarPorId(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Page<Rendimiento> buscarPorCuentaAhorroId(UUID cuentaAhorroId, Pageable pageable) {
        return jpaRepository.findByCuentaAhorroIdOrderByFechaCalculoDesc(cuentaAhorroId, pageable)
                .map(this::toDomain);
    }

    @Override
    public Optional<Rendimiento> buscarPorCuentaYPeriodo(UUID cuentaAhorroId,
            LocalDate periodoInicio, LocalDate periodoFin) {
        return jpaRepository.findByCuentaAndPeriodo(cuentaAhorroId, periodoInicio, periodoFin)
                .map(this::toDomain);
    }

    @Override
    public boolean existePorCuentaYPeriodoYTipo(UUID cuentaAhorroId, LocalDate periodoInicio,
            LocalDate periodoFin, TipoRendimiento tipo) {
        return jpaRepository.existsByCuentaAndPeriodoAndTipo(cuentaAhorroId, periodoInicio, periodoFin, tipo);
    }

    @Override
    public Page<Rendimiento> buscarPorEstado(EstadoAplicacion estado, Pageable pageable) {
        return jpaRepository.findByEstadoAplicacionOrderByFechaCalculoDesc(estado, pageable)
                .map(this::toDomain);
    }

    @Override
    public Page<Rendimiento> buscarPorRangoFechasAplicacion(LocalDate fechaInicio, 
            LocalDate fechaFin, Pageable pageable) {
        return jpaRepository.findByRangoFechasCalculo(fechaInicio, fechaFin, pageable)
                .map(this::toDomain);
    }

    private Rendimiento toDomain(RendimientoEntity entity) {
        return Rendimiento.builder()
                .id(entity.getId())
                .cuentaAhorroId(entity.getCuentaAhorroId())
                .periodoInicio(entity.getPeriodoInicio())
                .periodoFin(entity.getPeriodoFin())
                .saldoPromedioPeriodo(entity.getSaldoPromedioPeriodo())
                .tasaAplicada(entity.getTasaAplicada())
                .montoRendimiento(entity.getMontoRendimiento())
                .tipo(entity.getTipo())
                .estadoAplicacion(entity.getEstadoAplicacion())
                .fechaCalculo(entity.getFechaCalculo())
                .build();
    }

    private RendimientoEntity toEntity(Rendimiento rendimiento) {
        return RendimientoEntity.builder()
                .id(rendimiento.getId())
                .cuentaAhorroId(rendimiento.getCuentaAhorroId())
                .periodoInicio(rendimiento.getPeriodoInicio())
                .periodoFin(rendimiento.getPeriodoFin())
                .saldoPromedioPeriodo(rendimiento.getSaldoPromedioPeriodo())
                .tasaAplicada(rendimiento.getTasaAplicada())
                .montoRendimiento(rendimiento.getMontoRendimiento())
                .tipo(rendimiento.getTipo())
                .estadoAplicacion(rendimiento.getEstadoAplicacion())
                .fechaCalculo(rendimiento.getFechaCalculo())
                .build();
    }
}