// com/tufondo/creditos/domain/model/PlanAmortizacion.java
package com.tufondo.creditos.domain.model;

import com.tufondo.creditos.domain.model.enums.EstadoPlanAmortizacion;
import com.tufondo.creditos.domain.model.enums.FrecuenciaPago;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad PlanAmortizacion - Plan de amortización (sistema francés).
 * Genera el calendario de pagos con cuota fija.
 */
@Getter
@Setter
@Builder
public class PlanAmortizacion {
    private UUID id;
    private UUID solicitudId;
    private SolicitudCredito solicitud;  // Relación lazy
    private BigDecimal montoPrincipal;
    private BigDecimal tasaInteres;
    private Integer plazoMeses;
    private FrecuenciaPago frecuenciaPago;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private BigDecimal totalIntereses;
    private BigDecimal totalPagado;
    private BigDecimal saldoPendiente;
    private Integer numeroCuotas;
    private BigDecimal cuotaMensual;
    private EstadoPlanAmortizacion estado;
    private List<Amortizacion> cuotas;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;

    /**
     * Calcula la cuota mensual usando el sistema francés.
     * Fórmula: Cuota = P * (r * (1 + r)^n) / ((1 + r)^n - 1)
     */
    public static BigDecimal calcularCuotaFrances(BigDecimal principal, BigDecimal tasaAnual, int meses) {
        if (principal == null || tasaAnual == null || meses <= 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal tasaMensual = tasaAnual.divide(new BigDecimal("12"), 10, java.math.RoundingMode.HALF_UP);
        BigDecimal unoMasR = BigDecimal.ONE.add(tasaMensual);
        BigDecimal unoMasRN = unoMasR.pow(meses);
        
        BigDecimal numerador = principal.multiply(tasaMensual).multiply(unoMasRN);
        BigDecimal denominador = unoMasRN.subtract(BigDecimal.ONE);
        
        return numerador.divide(denominador, 4, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Genera las cuotas del plan de amortización (sistema francés).
     */
    public List<Amortizacion> generarCuotas() {
        if (cuotas == null) {
            cuotas = new ArrayList<>();
        }
        cuotas.clear();
        
        BigDecimal saldo = montoPrincipal;
        BigDecimal tasaMensual = tasaInteres.divide(new BigDecimal("12"), 10, java.math.RoundingMode.HALF_UP);
        LocalDate fechaVencimiento = fechaInicio;
        
        for (int i = 1; i <= numeroCuotas; i++) {
            // Calcular interés del período
            BigDecimal interes = saldo.multiply(tasaMensual).setScale(4, java.math.RoundingMode.HALF_UP);
            
            // Calcular capital de la cuota
            BigDecimal capital = cuotaMensual.subtract(interes);
            
            // Actualizar saldo
            saldo = saldo.subtract(capital).setScale(4, java.math.RoundingMode.HALF_UP);
            if (saldo.compareTo(BigDecimal.ZERO) < 0) {
                saldo = BigDecimal.ZERO;
            }
            
            // Avanzar fecha según frecuencia
            fechaVencimiento = calcularSiguienteFecha(fechaVencimiento, i);
            
            Amortizacion cuota = Amortizacion.builder()
                .id(UUID.randomUUID())
                .planId(this.id)
                .numeroCuota(i)
                .fechaVencimiento(fechaVencimiento)
                .capital(capital)
                .interes(interes)
                .seguros(BigDecimal.ZERO)
                .comisiones(BigDecimal.ZERO)
                .montoCuota(cuotaMensual)
                .saldoInsoluto(saldo)
                .montoPagado(BigDecimal.ZERO)
                .build();
            
            cuotas.add(cuota);
        }
        
        return cuotas;
    }

    private LocalDate calcularSiguienteFecha(LocalDate fechaActual, int numeroCuota) {
        return switch (frecuenciaPago) {
            case SEMANAL -> fechaActual.plusWeeks(1);
            case QUINCENAL -> fechaActual.plusDays(15);
            case MENSUAL -> fechaActual.plusMonths(1);
        };
    }

    /**
     * Registra un pago en el plan.
     */
    public void registrarPago(BigDecimal monto) {
        this.totalPagado = this.totalPagado.add(monto);
        this.saldoPendiente = this.saldoPendiente.subtract(monto);
    }

    /**
     * Verifica si el plan está completamente pagado.
     */
    public boolean estaCompletamentePagado() {
        return saldoPendiente != null && saldoPendiente.compareTo(BigDecimal.ZERO) <= 0;
    }

    /**
     * Calcula el total de intereses del plan.
     */
    public BigDecimal calcularTotalIntereses() {
        return cuotaMensual.multiply(new BigDecimal(numeroCuotas))
            .subtract(montoPrincipal);
    }

    /**
     * Marca el plan como finalizado.
     */
    public void marcarFinalizado() {
        this.estado = EstadoPlanAmortizacion.FINALIZADO;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Cuenta las cuotas pagadas.
     */
    public long countCuotasPagadas() {
        if (cuotas == null) return 0;
        return cuotas.stream()
            .filter(c -> c.getEstado() == com.tufondo.creditos.domain.model.enums.EstadoAmortizacion.PAGADA)
            .count();
    }

    /**
     * Cuenta las cuotas pendientes.
     */
    public long countCuotasPendientes() {
        if (cuotas == null) return 0;
        return cuotas.stream()
            .filter(c -> c.getEstado() == com.tufondo.creditos.domain.model.enums.EstadoAmortizacion.PENDIENTE)
            .count();
    }

    /**
     * Cuenta las cuotas vencidas.
     */
    public long countCuotasVencidas() {
        if (cuotas == null) return 0;
        return cuotas.stream()
            .filter(c -> c.getEstado() == com.tufondo.creditos.domain.model.enums.EstadoAmortizacion.VENCIDA ||
                         c.getEstado() == com.tufondo.creditos.domain.model.enums.EstadoAmortizacion.CURSO_MORA)
            .count();
    }

    /**
     * Crea un plan de amortización con el sistema francés.
     */
    public static PlanAmortizacion crearPlanFrances(UUID id, UUID solicitudId, 
            BigDecimal montoPrincipal, BigDecimal tasaInteres, int plazoMeses, LocalDate fechaInicio) {
        BigDecimal cuotaMensual = calcularCuotaFrances(montoPrincipal, tasaInteres, plazoMeses);
        BigDecimal totalIntereses = cuotaMensual.multiply(new BigDecimal(plazoMeses)).subtract(montoPrincipal);
        
        return PlanAmortizacion.builder()
            .id(id)
            .solicitudId(solicitudId)
            .montoPrincipal(montoPrincipal)
            .tasaInteres(tasaInteres)
            .plazoMeses(plazoMeses)
            .frecuenciaPago(FrecuenciaPago.MENSUAL)
            .fechaInicio(fechaInicio)
            .fechaFin(fechaInicio.plusMonths(plazoMeses))
            .numeroCuotas(plazoMeses)
            .cuotaMensual(cuotaMensual)
            .totalIntereses(totalIntereses.setScale(4, java.math.RoundingMode.HALF_UP))
            .totalPagado(BigDecimal.ZERO)
            .saldoPendiente(montoPrincipal)
            .estado(EstadoPlanAmortizacion.ACTIVO)
            .createdAt(LocalDateTime.now())
            .build();
    }
}
