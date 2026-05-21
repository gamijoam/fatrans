package com.tufondo.contabilidad.application.dto;

import com.tufondo.contabilidad.domain.model.enums.NaturalezaSaldo;
import com.tufondo.contabilidad.domain.model.enums.OrigenAsiento;
import com.tufondo.contabilidad.domain.model.enums.TipoCuentaContable;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Response del Libro Mayor (sub-issue #270).
 *
 * <p>Estructura: encabezado con datos de la entidad, lista de cuentas (cada
 * una con su saldo inicial, movimientos del período y saldo final), y
 * totales generales agregados.</p>
 */
@Builder
public record LibroMayorResponse(
        Encabezado encabezado,
        List<CuentaConMovimientos> cuentas,
        Totales totales
) {

    @Builder
    public record Encabezado(
            String razonSocial,
            String rif,
            LocalDate desde,
            LocalDate hasta,
            Instant generadoEn,
            UUID generadoPorUsuarioId,
            String filtroCuenta,            // null si todas las cuentas
            boolean incluyeSinMovimientos,
            boolean incluyeTotalizadoras
    ) {}

    /**
     * Una cuenta con su saldo inicial al comienzo del período, todos sus
     * movimientos del rango, y su saldo final calculado.
     */
    @Builder
    public record CuentaConMovimientos(
            String codigo,
            String nombre,
            TipoCuentaContable tipo,
            NaturalezaSaldo naturaleza,
            BigDecimal saldoInicialDebe,   // antes de aplicar naturaleza
            BigDecimal saldoInicialHaber,
            BigDecimal saldoInicialNeto,   // ya firmado según naturaleza
            String saldoInicialEtiqueta,   // "D" (deudor) / "A" (acreedor) / "—"
            List<MovimientoMayor> movimientos,
            BigDecimal totalDebePeriodo,
            BigDecimal totalHaberPeriodo,
            int cantidadMovimientos,
            BigDecimal saldoFinalDebe,
            BigDecimal saldoFinalHaber,
            BigDecimal saldoFinalNeto,
            String saldoFinalEtiqueta
    ) {}

    /**
     * Un movimiento del mayor: la partida de esta cuenta + datos del asiento
     * padre + contracuenta resuelta (la principal del lado opuesto).
     */
    @Builder
    public record MovimientoMayor(
            LocalDate fechaContable,
            long numeroAsiento,
            String numeroAsientoFormateado,   // "2026-000001"
            OrigenAsiento origen,
            String glosaAsiento,
            String referenciaExterna,
            String contracuentaCodigo,        // cuenta principal del lado opuesto
            String contracuentaNombre,
            String contracuentaResumen,       // "(múltiple)" si > 1 contracuenta
            BigDecimal debe,                  // 0 si esta partida es HABER
            BigDecimal haber,                 // 0 si esta partida es DEBE
            BigDecimal saldoAcumulado         // saldo después de esta partida
    ) {}

    @Builder
    public record Totales(
            int cantidadCuentas,
            int cantidadMovimientos,
            BigDecimal totalDebe,
            BigDecimal totalHaber,
            boolean balanceado
    ) {}
}
