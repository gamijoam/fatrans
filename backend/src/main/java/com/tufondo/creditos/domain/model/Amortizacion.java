// com/tufondo/creditos/domain/model/Amortizacion.java
package com.tufondo.creditos.domain.model;

import com.tufondo.creditos.domain.model.enums.EstadoAmortizacion;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad Amortizacion - Cuota individual del plan de amortización.
 * Incluye prevención de double-payment con @Version y referenciaPago única.
 */
@Getter
@Setter
@Builder
public class Amortizacion {
    private UUID id;
    private UUID planId;
    private PlanAmortizacion plan;  // Relación lazy
    private Integer numeroCuota;
    private LocalDate fechaVencimiento;
    private LocalDate fechaPago;
    private BigDecimal capital;
    private BigDecimal interes;
    private BigDecimal seguros;
    private BigDecimal comisiones;
    private BigDecimal montoCuota;
    private BigDecimal saldoInsoluto;
    private EstadoAmortizacion estado;
    private Integer diasMora;
    private BigDecimal interesMora;
    private BigDecimal montoPagado;
    private String referenciaPago;  // Clave de idempotencia
    private Boolean colateralEjecutada;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;

    /**
     * Registra el pago de la cuota.
     * @throws IllegalStateException si la cuota no está en estado válido para pagar
     */
    public void registrarPago(BigDecimal monto, LocalDate fecha, String referencia) {
        if (this.estado != EstadoAmortizacion.PENDIENTE && 
            this.estado != EstadoAmortizacion.VENCIDA) {
            throw new IllegalStateException(
                String.format("Cuota en estado %s no puede ser pagada", this.estado)
            );
        }
        
        this.montoPagado = monto;
        this.fechaPago = fecha;
        this.referenciaPago = referencia;
        this.estado = EstadoAmortizacion.PAGADA;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Calcula los días de mora desde el vencimiento.
     */
    public int calcularDiasMora() {
        if (fechaVencimiento == null || LocalDate.now().isBefore(fechaVencimiento)) {
            return 0;
        }
        return (int) java.time.temporal.ChronoUnit.DAYS.between(fechaVencimiento, LocalDate.now());
    }

    /**
     * Calcula el interés moratorio diario.
     * Formula: saldo_pendiente * (tasa_mora_diaria)
     */
    public BigDecimal calcularInteresMora(BigDecimal tasaMoraDiaria) {
        BigDecimal saldoPendiente = montoCuota.subtract(montoPagado != null ? montoPagado : BigDecimal.ZERO);
        return saldoPendiente.multiply(tasaMoraDiaria);
    }

    /**
     * Marca la cuota como vencida.
     */
    public void marcarVencida() {
        this.estado = EstadoAmortizacion.VENCIDA;
        this.diasMora = calcularDiasMora();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Marca la cuota como en curso de mora (> 30 días).
     */
    public void marcarCursoMora() {
        this.estado = EstadoAmortizacion.CURSO_MORA;
        this.diasMora = calcularDiasMora();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Marca la cuota como ejecutada (colateral).
     */
    public void marcarEjecutada(BigDecimal montoEjecutado, int diasMora) {
        this.estado = EstadoAmortizacion.EJECUTADA;
        this.montoPagado = montoEjecutado;
        this.diasMora = diasMora;
        this.colateralEjecutada = true;
        this.fechaPago = LocalDate.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Verifica si la cuota está vencida (pasó fechaVencimiento).
     */
    public boolean estaVencida() {
        return LocalDate.now().isAfter(fechaVencimiento) && 
               this.estado == EstadoAmortizacion.PENDIENTE;
    }

    /**
     * Verifica si debe entrar en curso de mora (> 30 días vencida).
     */
    public boolean debeEntrarEnMora() {
        return diasMora != null && diasMora > 30 && 
               this.estado == EstadoAmortizacion.VENCIDA;
    }

    /**
     * Verifica si puede ejecutar el colateral (> 90 días vencida).
     */
    public boolean puedeEjecutarColateral() {
        return diasMora != null && diasMora > 90 && 
               this.estado == EstadoAmortizacion.CURSO_MORA;
    }

    /**
     * Calcula el monto total a pagar (cuota + intereses mora si aplica).
     */
    public BigDecimal getMontoTotalAPagar() {
        BigDecimal total = montoCuota;
        if (interesMora != null && interesMora.compareTo(BigDecimal.ZERO) > 0) {
            total = total.add(interesMora);
        }
        return total;
    }

    /**
     * Obtiene el saldo pendiente de la cuota.
     */
    public BigDecimal getSaldoPendiente() {
        if (montoPagado == null) {
            return montoCuota;
        }
        return montoCuota.subtract(montoPagado);
    }
}
