// com.tufondo.creditos.domain.repository.CuentaGarantiaRepository.java
package com.tufondo.creditos.domain.repository;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Repository interface para CuentaGarantia (cuenta de ahorro como colateral).
 * Esta interfaz está en el dominio de Créditos pero la implementación
 * está en la capa de infraestructura, desacoplando los módulos.
 */
public interface CuentaGarantiaRepository {
    
    /**
     * Obtiene el saldo disponible para garantía.
     * @param cuentaId ID de la cuenta de ahorro
     * @return saldo disponible (total - retenido)
     */
    BigDecimal obtenerSaldoDisponible(UUID cuentaId);
    
    /**
     * Verifica si el saldo disponible cubre el requerimiento.
     * @param cuentaId ID de la cuenta
     * @param montoRequerido Monto requerido como colateral
     * @return true si cubre, false si no
     */
    boolean verificarSaldoParaColateral(UUID cuentaId, BigDecimal montoRequerido);
    
    /**
     * Retiene saldo para colateral (al aprobar crédito).
     * @param cuentaId ID de la cuenta
     * @param monto Monto a retener
     */
    void retenerSaldo(UUID cuentaId, BigDecimal monto);
    
    /**
     * Libera saldo retenido (al pagar última cuota o ejecutar colateral).
     * @param cuentaId ID de la cuenta
     * @param monto Monto a liberar
     */
    void liberarSaldo(UUID cuentaId, BigDecimal monto);
    
    /**
     * Transfiere saldo (para ejecución de colateral).
     * @param cuentaOrigenId Cuenta de donde se toma
     * @param cuentaDestinoId Cuenta a donde se transfiere
     * @param monto Monto a transferir
     */
    void transferirSaldo(UUID cuentaOrigenId, UUID cuentaDestinoId, BigDecimal monto);
}
