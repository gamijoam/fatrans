// com/tufondo/ahorros/domain/model/Movimiento.java
package com.tufondo.ahorros.domain.model;

import com.tufondo.ahorros.domain.model.enums.CanalOrigen;
import com.tufondo.ahorros.domain.model.enums.EstadoMovimiento;
import com.tufondo.ahorros.domain.model.enums.TipoMovimiento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad Movimiento financiero.
 * RN-006: Movimientos son INMUTABLES una vez creados.
 * RN-007: numeroOperacion formato MOV-YYYY-XXXXXX.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movimiento {
    private UUID id;
    private String numeroOperacion;  // Formato: MOV-YYYY-XXXXXX
    private UUID cuentaAhorroId;
    private UUID socioId;
    private TipoMovimiento tipo;
    private BigDecimal monto;
    private BigDecimal saldoAnterior;
    private BigDecimal saldoPosterior;
    private String descripcion;
    private String referencia;
    private CanalOrigen canalOrigen;
    private String ipOrigen;
    private String sessionId;
    private String requestId;
    private EstadoMovimiento estado;
    private LocalDateTime fechaMovimiento;
    private LocalDate fechaValor;

    /**
     * Verifica si el movimiento está en estado procesable.
     */
    public boolean estaProcesado() {
        return estado == EstadoMovimiento.PROCESADO;
    }

    /**
     * Crea un movimiento de depósito.
     */
    public static Movimiento crearDeposito(UUID cuentaAhorroId, UUID socioId, 
            String numeroOperacion, BigDecimal monto, BigDecimal saldoAnterior,
            BigDecimal saldoPosterior, CanalOrigen canalOrigen, String ipOrigen,
            String sessionId, String requestId, String descripcion, String referencia) {
        return Movimiento.builder()
                .numeroOperacion(numeroOperacion)
                .cuentaAhorroId(cuentaAhorroId)
                .socioId(socioId)
                .tipo(TipoMovimiento.DEPOSITO)
                .monto(monto)
                .saldoAnterior(saldoAnterior)
                .saldoPosterior(saldoPosterior)
                .descripcion(descripcion)
                .referencia(referencia)
                .canalOrigen(canalOrigen)
                .ipOrigen(ipOrigen)
                .sessionId(sessionId)
                .requestId(requestId)
                .estado(EstadoMovimiento.PROCESADO)
                .fechaMovimiento(LocalDateTime.now())
                .fechaValor(LocalDate.now())
                .build();
    }

    /**
     * Crea un movimiento de retiro.
     */
    public static Movimiento crearRetiro(UUID cuentaAhorroId, UUID socioId,
            String numeroOperacion, BigDecimal monto, BigDecimal saldoAnterior,
            BigDecimal saldoPosterior, CanalOrigen canalOrigen, String ipOrigen,
            String sessionId, String requestId) {
        return Movimiento.builder()
                .numeroOperacion(numeroOperacion)
                .cuentaAhorroId(cuentaAhorroId)
                .socioId(socioId)
                .tipo(TipoMovimiento.RETIRO)
                .monto(monto)
                .saldoAnterior(saldoAnterior)
                .saldoPosterior(saldoPosterior)
                .canalOrigen(canalOrigen)
                .ipOrigen(ipOrigen)
                .sessionId(sessionId)
                .requestId(requestId)
                .estado(EstadoMovimiento.PROCESADO)
                .fechaMovimiento(LocalDateTime.now())
                .fechaValor(LocalDate.now())
                .build();
    }

    /**
     * Crea un movimiento de rendimiento (interés acreditado).
     */
    public static Movimiento crearRendimiento(UUID cuentaAhorroId, UUID socioId,
            String numeroOperacion, BigDecimal monto, BigDecimal saldoAnterior,
            BigDecimal saldoPosterior, String requestId) {
        return Movimiento.builder()
                .numeroOperacion(numeroOperacion)
                .cuentaAhorroId(cuentaAhorroId)
                .socioId(socioId)
                .tipo(TipoMovimiento.INTERES_CREDITO)
                .monto(monto)
                .saldoAnterior(saldoAnterior)
                .saldoPosterior(saldoPosterior)
                .descripcion("Rendimiento aplicado")
                .canalOrigen(CanalOrigen.BATCH)
                .requestId(requestId)
                .estado(EstadoMovimiento.PROCESADO)
                .fechaMovimiento(LocalDateTime.now())
                .fechaValor(LocalDate.now())
                .build();
    }
}