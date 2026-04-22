// com/tufondo/ahorros/domain/repository/RendimientoRepository.java
package com.tufondo.ahorros.domain.repository;

import com.tufondo.ahorros.domain.model.Rendimiento;
import com.tufondo.ahorros.domain.model.enums.EstadoAplicacion;
import com.tufondo.ahorros.domain.model.enums.TipoRendimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface para Rendimiento.
 */
public interface RendimientoRepository {
    
    /**
     * Guarda un rendimiento.
     */
    Rendimiento guardar(Rendimiento rendimiento);
    
    /**
     * Busca rendimiento por ID.
     */
    Optional<Rendimiento> buscarPorId(UUID id);
    
    /**
     * Lista rendimientos por cuenta.
     */
    Page<Rendimiento> buscarPorCuentaAhorroId(UUID cuentaAhorroId, Pageable pageable);
    
    /**
     * Busca rendimiento por cuenta y periodo.
     * RN-012: Un periodo no puede ser recalculado si ya fue aplicado.
     */
    Optional<Rendimiento> buscarPorCuentaYPeriodo(UUID cuentaAhorroId,
            LocalDate periodoInicio, LocalDate periodoFin);
    
    /**
     * Verifica si existe rendimiento calculado para el periodo.
     */
    boolean existePorCuentaYPeriodoYTipo(UUID cuentaAhorroId, LocalDate periodoInicio,
            LocalDate periodoFin, TipoRendimiento tipo);
    
    /**
     * Lista rendimientos por estado de aplicación.
     */
    Page<Rendimiento> buscarPorEstado(EstadoAplicacion estado, Pageable pageable);
    
    /**
     * Lista rendimientos aplicados en un rango de fechas.
     */
    Page<Rendimiento> buscarPorRangoFechasAplicacion(LocalDate fechaInicio, 
            LocalDate fechaFin, Pageable pageable);
}