// com/tufondo/ahorros/domain/repository/CuentaAhorroRepository.java
package com.tufondo.ahorros.domain.repository;

import com.tufondo.ahorros.domain.model.CuentaAhorro;
import com.tufondo.ahorros.domain.model.enums.EstadoCuenta;
import com.tufondo.ahorros.domain.model.enums.TipoCuenta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface para CuentaAhorro.
 * Sigue el patrón de Repository del módulo socios.
 */
public interface CuentaAhorroRepository {
    
    /**
     * Guarda una cuenta de ahorro.
     */
    CuentaAhorro guardar(CuentaAhorro cuenta);
    
    /**
     * Busca cuenta por ID.
     */
    Optional<CuentaAhorro> buscarPorId(UUID id);
    
    /**
     * Busca cuenta por número de cuenta.
     */
    Optional<CuentaAhorro> buscarPorNumeroCuenta(String numeroCuenta);
    
    /**
     * Lista cuentas por socio.
     */
    Page<CuentaAhorro> buscarPorSocioId(UUID socioId, Pageable pageable);

    /**
     * Verifica si existe cuenta para socio y tipo.
     * RN-001: Un socio solo puede tener una cuenta por tipo.
     */
    boolean existePorSocioIdYTipo(UUID socioId, TipoCuenta tipoCuenta);
    
    /**
     * Lista cuentas por estado.
     */
    Page<CuentaAhorro> buscarPorEstado(EstadoCuenta estado, Pageable pageable);
    
    /**
     * Lista cuentas activas para cálculo de rendimientos.
     */
    Page<CuentaAhorro> buscarCuentasActivas(Pageable pageable);
    
    /**
     * Verifica si existe número de cuenta.
     * Usado para evitar duplicados en generación de números.
     */
    boolean existePorNumeroCuenta(String numeroCuenta);

    long count();
    long countByEstado(EstadoCuenta estado);
    BigDecimal sumSaldoActualCuentasActivas();
}
