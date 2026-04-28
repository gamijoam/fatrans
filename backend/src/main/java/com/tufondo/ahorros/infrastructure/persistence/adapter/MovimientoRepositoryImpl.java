// com/tufondo/ahorros/infrastructure/persistence/adapter/MovimientoRepositoryImpl.java
package com.tufondo.ahorros.infrastructure.persistence.adapter;

import com.tufondo.ahorros.domain.model.Movimiento;
import com.tufondo.ahorros.domain.model.enums.EstadoMovimiento;
import com.tufondo.ahorros.domain.model.enums.TipoMovimiento;
import com.tufondo.ahorros.domain.repository.MovimientoRepository;
import com.tufondo.ahorros.infrastructure.persistence.entity.MovimientoEntity;
import com.tufondo.ahorros.infrastructure.persistence.jpa.MovimientoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementación del repositorio de Movimiento.
 * RN-006: Movimientos son INMUTABLES una vez creados.
 */
@Repository
@RequiredArgsConstructor
public class MovimientoRepositoryImpl implements MovimientoRepository {

    private final MovimientoJpaRepository jpaRepository;

    @Override
    public Movimiento guardar(Movimiento movimiento) {
        MovimientoEntity entity = toEntity(movimiento);
        entity = jpaRepository.save(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<Movimiento> buscarPorId(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Movimiento> buscarPorNumeroOperacion(String numeroOperacion) {
        return jpaRepository.findByNumeroOperacion(numeroOperacion).map(this::toDomain);
    }

    @Override
    public Page<Movimiento> buscarPorCuentaAhorroId(UUID cuentaAhorroId, Pageable pageable) {
        return jpaRepository.findByCuentaAhorroIdOrderByFechaMovimientoDesc(cuentaAhorroId, pageable)
                .map(this::toDomain);
    }

    @Override
    public Page<Movimiento> buscarPorCuentaYRangoFechas(UUID cuentaAhorroId,
            LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable) {
        return jpaRepository.findByCuentaYRangoFechas(cuentaAhorroId, fechaInicio, fechaFin, pageable)
                .map(this::toDomain);
    }

    @Override
    public Page<Movimiento> buscarPorCuentaYTipo(UUID cuentaAhorroId, TipoMovimiento tipo, Pageable pageable) {
        return jpaRepository.findByCuentaAhorroIdAndTipoOrderByFechaMovimientoDesc(cuentaAhorroId, tipo, pageable)
                .map(this::toDomain);
    }

    @Override
    public Page<Movimiento> buscarPorCuentaYRangoFechasYTipo(UUID cuentaAhorroId,
            LocalDateTime fechaInicio, LocalDateTime fechaFin, TipoMovimiento tipo, Pageable pageable) {
        return jpaRepository.findByCuentaYRangoFechasYTipo(cuentaAhorroId, fechaInicio, fechaFin, tipo, pageable)
                .map(this::toDomain);
    }

    @Override
    public Page<Movimiento> buscarPorSocioId(UUID socioId, Pageable pageable) {
        return jpaRepository.findBySocioIdOrderByFechaMovimientoDesc(socioId, pageable)
                .map(this::toDomain);
    }

    @Override
    public BigDecimal sumRetirosDelDiaPorSocio(UUID socioId, LocalDateTime inicioDia) {
        return jpaRepository.sumMontoBySocioIdAndTipoAndFechaMovimientoAfterAndEstado(
                socioId, TipoMovimiento.RETIRO, inicioDia, EstadoMovimiento.PROCESADO);
    }

    @Override
    public long contarPorCuentaYEstado(UUID cuentaAhorroId, EstadoMovimiento estado) {
        return jpaRepository.countByCuentaAhorroIdAndEstado(cuentaAhorroId, estado);
    }
    
    @Override
    public boolean existePorNumeroOperacion(String numeroOperacion) {
        return jpaRepository.existsByNumeroOperacion(numeroOperacion);
    }

    @Override
    public BigDecimal sumDepositosMes(LocalDateTime inicioMes) {
        return jpaRepository.sumDepositosMes(inicioMes);
    }

    @Override
    public BigDecimal sumRetirosMes(LocalDateTime inicioMes) {
        return jpaRepository.sumRetirosMes(inicioMes);
    }

    @Override
    public long countByTipoAndFechaAfter(TipoMovimiento tipo, LocalDateTime fecha) {
        return jpaRepository.countByTipoAndFechaAfter(tipo, fecha);
    }

    private Movimiento toDomain(MovimientoEntity entity) {
        return Movimiento.builder()
                .id(entity.getId())
                .numeroOperacion(entity.getNumeroOperacion())
                .cuentaAhorroId(entity.getCuentaAhorroId())
                .socioId(entity.getSocioId())
                .tipo(entity.getTipo())
                .monto(entity.getMonto())
                .saldoAnterior(entity.getSaldoAnterior())
                .saldoPosterior(entity.getSaldoPosterior())
                .descripcion(entity.getDescripcion())
                .referencia(entity.getReferencia())
                .canalOrigen(entity.getCanalOrigen())
                .ipOrigen(entity.getIpOrigen())
                .sessionId(entity.getSessionId())
                .requestId(entity.getRequestId())
                .estado(entity.getEstado())
                .fechaMovimiento(entity.getFechaMovimiento())
                .fechaValor(entity.getFechaValor())
                .build();
    }

    private MovimientoEntity toEntity(Movimiento movimiento) {
        return MovimientoEntity.builder()
                .id(movimiento.getId())
                .numeroOperacion(movimiento.getNumeroOperacion())
                .cuentaAhorroId(movimiento.getCuentaAhorroId())
                .socioId(movimiento.getSocioId())
                .tipo(movimiento.getTipo())
                .monto(movimiento.getMonto())
                .saldoAnterior(movimiento.getSaldoAnterior())
                .saldoPosterior(movimiento.getSaldoPosterior())
                .descripcion(movimiento.getDescripcion())
                .referencia(movimiento.getReferencia())
                .canalOrigen(movimiento.getCanalOrigen())
                .ipOrigen(movimiento.getIpOrigen())
                .sessionId(movimiento.getSessionId())
                .requestId(movimiento.getRequestId())
                .estado(movimiento.getEstado())
                .fechaMovimiento(movimiento.getFechaMovimiento())
                .fechaValor(movimiento.getFechaValor())
                .build();
    }
}