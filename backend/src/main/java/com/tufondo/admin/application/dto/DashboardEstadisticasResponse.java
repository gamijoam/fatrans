package com.tufondo.admin.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardEstadisticasResponse {

    private long totalSocios;
    private long sociosActivos;
    private long sociosInactivos;
    private long sociosPendientes;

    private BigDecimal totalAportaciones;
    private long totalCuentasAhorro;
    private long cuentasActivas;
    private long cuentasSuspendidas;
    private BigDecimal depositosMes;
    private BigDecimal retirosMes;

    private long prestamosActivos;
    private long solicitudesPendientes;
    private long solicitudesAprobadas;
    private long solicitudesRechazadas;
    private BigDecimal capitalDesembolsado;
    private BigDecimal carteraVencida;

    /**
     * Solicitudes de registro de socios pendientes de aprobar por un admin.
     * Antes el card "Solicitudes Pendientes" del dashboard mostraba el conteo
     * de solicitudes de crédito (`solicitudesPendientes`), no el de registro —
     * por eso aparecía 0 aunque hubiera nuevas solicitudes esperando.
     */
    private long solicitudesRegistroPendientes;

    private long cuotasVencidas;
    private long cuotasEnMora;
    private long cuotasPagadas;
    private BigDecimal interesesMoraGenerados;

    private double tasaCumplimiento;
    private double tasaMora;
    private BigDecimal rendimientoPromedio;

    private ActividadRecienteResponse actividadReciente;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ActividadRecienteResponse {
        private long nuevosSociosMes;
        private long depositosMes;
        private long retirosMes;
        private long prestamosAprobadosMes;
        private long prestamosDesembolsadosMes;
        private BigDecimal montoDepositadoMes;
        private BigDecimal montoRetiradoMes;
    }
}