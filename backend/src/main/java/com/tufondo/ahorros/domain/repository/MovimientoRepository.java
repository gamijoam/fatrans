// com/tufondo/ahorros/domain/repository/MovimientoRepository.java
package com.tufondo.ahorros.domain.repository;

import com.tufondo.ahorros.domain.model.Movimiento;
import com.tufondo.ahorros.domain.model.enums.EstadoMovimiento;
import com.tufondo.ahorros.domain.model.enums.TipoMovimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface para Movimiento.
 */
public interface MovimientoRepository {
    
    /**
     * Guarda un movimiento.
     * RN-006: Movimientos son INMUTABLES una vez creados.
     */
    Movimiento guardar(Movimiento movimiento);
    
    /**
     * Busca movimiento por ID.
     */
    Optional<Movimiento> buscarPorId(UUID id);
    
    /**
     * Busca movimiento por número de operación.
     */
    Optional<Movimiento> buscarPorNumeroOperacion(String numeroOperacion);
    
    /**
     * Lista movimientos por cuenta con paginación.
     */
    Page<Movimiento> buscarPorCuentaAhorroId(UUID cuentaAhorroId, Pageable pageable);
    
    /**
     * Lista movimientos por cuenta y rango de fechas.
     */
    Page<Movimiento> buscarPorCuentaYRangoFechas(UUID cuentaAhorroId,
            LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);
    
    /**
     * Lista movimientos por cuenta y tipo.
     */
    Page<Movimiento> buscarPorCuentaYTipo(UUID cuentaAhorroId, TipoMovimiento tipo, Pageable pageable);

    /**
     * Lista movimientos por cuenta, rango de fechas y tipo.
     */
    Page<Movimiento> buscarPorCuentaYRangoFechasYTipo(UUID cuentaAhorroId,
            LocalDateTime fechaInicio, LocalDateTime fechaFin, TipoMovimiento tipo, Pageable pageable);

    /**
     * Lista movimientos por socio.
     */
    Page<Movimiento> buscarPorSocioId(UUID socioId, Pageable pageable);

    /**
     * Suma de retiros del día para un socio.
     * Límite: 50,000 MXN/día.
     */
    BigDecimal sumRetirosDelDiaPorSocio(UUID socioId, LocalDateTime inicioDia);
    
    /**
     * Cuenta movimientos pendientes de una cuenta.
     */
    long contarPorCuentaYEstado(UUID cuentaAhorroId, EstadoMovimiento estado);
    
    /**
     * Verifica si existe número de operación.
     * Usado para evitar duplicados en generación de números.
     */
    boolean existePorNumeroOperacion(String numeroOperacion);

    BigDecimal sumDepositosMes(LocalDateTime inicioMes);
    BigDecimal sumRetirosMes(LocalDateTime inicioMes);
    long countByTipoAndFechaAfter(TipoMovimiento tipo, LocalDateTime fecha);
}
