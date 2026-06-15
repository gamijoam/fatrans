// com/tufondo/ahorros/domain/model/CuentaAhorro.java
package com.tufondo.ahorros.domain.model;

import com.tufondo.ahorros.domain.model.enums.EstadoCuenta;
import com.tufondo.ahorros.domain.model.enums.Moneda;
import com.tufondo.ahorros.domain.model.enums.TipoCuenta;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad aggregate root para Cuenta de Ahorro.
 * RN-001: Un socio solo puede tener una cuenta por tipo.
 * RN-003: saldoActual nunca negativo.
 * RN-004: montoMinimoRequerido >= 0.0001.
 * RN-005: Cuenta CERRADA no permite operaciones.
 */
@Getter
@Builder
public class CuentaAhorro {
    private UUID id;
    private String numeroCuenta;  // Formato: AHO-YYYY-XXXXXX
    private UUID socioId;
    private BigDecimal saldoActual;
    private BigDecimal saldoRetenido;
    private BigDecimal tasaInteres;
    private BigDecimal montoMinimoRequerido;
    private EstadoCuenta estado;
    private TipoCuenta tipoCuenta;
    private Moneda moneda;
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaUltimaOperacion;
    private Long version;

    /**
     * Calcula el saldo disponible (saldoActual - saldoRetenido).
     */
    public BigDecimal getSaldoDisponible() {
        if (saldoActual == null) return BigDecimal.ZERO;
        if (saldoRetenido == null) return saldoActual;
        return saldoActual.subtract(saldoRetenido);
    }

    /**
     * Verifica si la cuenta permite operaciones.
     * RN-005: Solo cuentas ACTIVA pueden operar.
     */
    public boolean permiteOperaciones() {
        return estado == EstadoCuenta.ACTIVA;
    }

    /**
     * Verifica si el saldo puede cubrir un monto específico.
     */
    public boolean tieneSaldoSuficiente(BigDecimal monto) {
        return getSaldoDisponible().compareTo(monto) >= 0;
    }

    /**
     * Verifica si el retiro resultaría en saldo menor al mínimo requerido.
     */
    public boolean resultariaEnSaldoBajoMinimo(BigDecimal monto) {
        BigDecimal saldoResultante = getSaldoDisponible().subtract(monto);
        return saldoResultante.compareTo(montoMinimoRequerido) < 0;
    }

    /**
     * Agrega monto al saldo (depósito).
     */
    public void agregarSaldo(BigDecimal monto) {
        if (saldoActual == null) {
            saldoActual = BigDecimal.ZERO;
        }
        saldoActual = saldoActual.add(monto);
        fechaUltimaOperacion = LocalDateTime.now();
    }

    /**
     * Resta monto del saldo (retiro). 
     * RN-003: No permite saldo negativo.
     */
    public void restarSaldo(BigDecimal monto) {
        if (saldoActual == null) {
            throw new IllegalStateException("Saldo no puede ser null");
        }
        if (saldoActual.subtract(monto).compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Saldo no puede ser negativo (RN-003)");
        }
        saldoActual = saldoActual.subtract(monto);
        fechaUltimaOperacion = LocalDateTime.now();
    }

    public void retenerSaldo(BigDecimal monto) {
        if (saldoRetenido == null) {
            saldoRetenido = BigDecimal.ZERO;
        }
        if (!tieneSaldoSuficiente(monto)) {
            throw new IllegalStateException("Saldo disponible insuficiente para retener");
        }
        saldoRetenido = saldoRetenido.add(monto);
        fechaUltimaOperacion = LocalDateTime.now();
    }

    public void liberarSaldoRetenido(BigDecimal monto) {
        if (saldoRetenido == null) {
            saldoRetenido = BigDecimal.ZERO;
        }
        if (saldoRetenido.subtract(monto).compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Saldo retenido no puede ser negativo");
        }
        saldoRetenido = saldoRetenido.subtract(monto);
        fechaUltimaOperacion = LocalDateTime.now();
    }

    /**
     * Cierra la cuenta cambiando estado a CERRADA.
     */
    public void cerrar() {
        this.estado = EstadoCuenta.CERRADA;
        this.fechaUltimaOperacion = LocalDateTime.now();
    }

    /**
     * Valida que la cuenta puede ser cerrada (saldo debe ser cero).
     */
    public boolean puedeCerrarse() {
        return saldoActual != null && saldoActual.compareTo(BigDecimal.ZERO) == 0;
    }
}
