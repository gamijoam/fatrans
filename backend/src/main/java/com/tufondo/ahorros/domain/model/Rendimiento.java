// com/tufondo/ahorros/domain/model/Rendimiento.java
package com.tufondo.ahorros.domain.model;

import com.tufondo.ahorros.domain.model.enums.EstadoAplicacion;
import com.tufondo.ahorros.domain.model.enums.TipoRendimiento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad Rendimiento financiero.
 * RN-010: tasaAplicada debe estar en rango 0.0001 - 1.0 (CRÍTICO overflow).
 * RN-011: saldoPromedioPeriodo requiere al menos 30 días de historia.
 * RN-012: Un periodo no puede ser recalculado si ya fue aplicado.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rendimiento {
    private UUID id;
    private UUID cuentaAhorroId;
    private LocalDate periodoInicio;
    private LocalDate periodoFin;
    private BigDecimal saldoPromedioPeriodo;
    private BigDecimal tasaAplicada;
    private BigDecimal montoRendimiento;
    private TipoRendimiento tipo;
    private EstadoAplicacion estadoAplicacion;
    private LocalDateTime fechaCalculo;

    /**
     * Verifica si el rendimiento está en estado calculado (pendiente de aplicar).
     */
    public boolean estaCalculado() {
        return estadoAplicacion == EstadoAplicacion.CALCULADO;
    }

    /**
     * Verifica si el rendimiento ya fue aplicado.
     */
    public boolean estaAplicado() {
        return estadoAplicacion == EstadoAplicacion.APLICADO;
    }

    /**
     * Marca el rendimiento como aplicado.
     */
    public void marcarComoAplicado() {
        this.estadoAplicacion = EstadoAplicacion.APLICADO;
    }

    /**
     * Marca el rendimiento como cancelado.
     */
    public void cancelar() {
        this.estadoAplicacion = EstadoAplicacion.CANCELADO;
    }

    /**
     * Valida que la tasa esté en rango válido.
     * RN-010: CRÍTICO - Validación de overflow.
     */
    public static boolean esTasaValida(BigDecimal tasa) {
        if (tasa == null) return false;
        BigDecimal min = new BigDecimal("0.0001");
        BigDecimal max = BigDecimal.ONE;
        return tasa.compareTo(min) >= 0 && tasa.compareTo(max) <= 0;
    }

    /**
     * Calcula el rendimiento basado en saldo promedio y tasa anual prorateado.
     */
    public static BigDecimal calcularMontoRendimiento(BigDecimal saldoPromedio, 
            BigDecimal tasaAnual, int diasPeriodo) {
        // Fórmula: (saldo * tasa * dias) / 365
        // Validamos overflow con límites
        if (!esTasaValida(tasaAnual)) {
            throw new IllegalArgumentException("Tasa inválida: debe estar entre 0.0001 y 1.0 (RN-010)");
        }
        BigDecimal diasAnio = new BigDecimal("365");
        BigDecimal tasaDiaria = tasaAnual.divide(diasAnio, 10, java.math.RoundingMode.HALF_UP);
        return saldoPromedio.multiply(tasaDiaria).multiply(new BigDecimal(diasPeriodo))
                .setScale(4, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Crea un nuevo rendimiento calculado.
     */
    public static Rendimiento crear(UUID cuentaAhorroId, LocalDate periodoInicio,
            LocalDate periodoFin, BigDecimal saldoPromedio, BigDecimal tasaAplicada,
            TipoRendimiento tipo) {
        
        if (!esTasaValida(tasaAplicada)) {
            throw new IllegalArgumentException("Tasa inválida para rendimiento (RN-010)");
        }
        
        int dias = (int) java.time.temporal.ChronoUnit.DAYS.between(periodoInicio, periodoFin) + 1;
        BigDecimal monto = calcularMontoRendimiento(saldoPromedio, tasaAplicada, dias);
        
        return Rendimiento.builder()
                .cuentaAhorroId(cuentaAhorroId)
                .periodoInicio(periodoInicio)
                .periodoFin(periodoFin)
                .saldoPromedioPeriodo(saldoPromedio)
                .tasaAplicada(tasaAplicada)
                .montoRendimiento(monto)
                .tipo(tipo)
                .estadoAplicacion(EstadoAplicacion.CALCULADO)
                .fechaCalculo(LocalDateTime.now())
                .build();
    }
}