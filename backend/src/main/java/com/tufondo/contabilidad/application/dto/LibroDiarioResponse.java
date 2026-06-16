package com.tufondo.contabilidad.application.dto;

import com.tufondo.contabilidad.domain.model.enums.EstadoAsiento;
import com.tufondo.contabilidad.domain.model.enums.OrigenAsiento;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Response del Libro Diario (sub-issue #269).
 *
 * <p>Estructura tradicional contable: encabezado con datos de la entidad y
 * período, lista de asientos con sus partidas, y totales del período. El
 * frontend o el adapter PDF consumen este mismo DTO.</p>
 */
@Builder
public record LibroDiarioResponse(
        Encabezado encabezado,
        List<AsientoDiario> asientos,
        Totales totales
) {

    /**
     * Datos identificatorios y de auditoría que cabecean el reporte.
     * Razón social y RIF vienen de properties — ver {@code EntidadProperties}.
     */
    @Builder
    public record Encabezado(
            String razonSocial,
            String rif,
            LocalDate desde,
            LocalDate hasta,
            Instant generadoEn,
            UUID generadoPorUsuarioId,
            boolean incluyeAnulados
    ) {}

    /**
     * Un asiento del libro, con su número formateado para presentación
     * ({@code AÑO-NNNNNN}, ej. "2026-000001") y sus partidas en orden.
     */
    @Builder
    public record AsientoDiario(
            long numero,
            String numeroFormateado,       // "2026-000123" (año + correlativo)
            LocalDate fechaContable,
            OrigenAsiento origen,
            EstadoAsiento estado,
            String glosa,
            String referenciaExterna,
            String motivoAnulacion,         // null si REGISTRADO
            List<PartidaDiario> partidas,
            BigDecimal totalDebe,
            BigDecimal totalHaber
    ) {}

    /**
     * Una partida (renglón) del asiento. Incluye nombre de cuenta (resuelto
     * por el use case via lookup en el plan) para que el reporte sea
     * autocontenido y no requiera otras llamadas.
     */
    @Builder
    public record PartidaDiario(
            String codigoCuenta,
            String nombreCuenta,
            BigDecimal debe,
            BigDecimal haber,
            String glosa,
            int orden
    ) {}

    /**
     * Totales agregados del período. Si {@code balanceado=false} hay un
     * problema serio — sería bug del sistema porque cada asiento individual
     * está balanceado por invariante de dominio.
     */
    @Builder
    public record Totales(
            int cantidadAsientos,
            int cantidadAnulados,
            BigDecimal totalDebe,
            BigDecimal totalHaber,
            boolean balanceado
    ) {}
}
