package com.tufondo.contabilidad.application.dto;

import com.tufondo.contabilidad.domain.model.enums.NaturalezaSaldo;
import com.tufondo.contabilidad.domain.model.enums.TipoCuentaContable;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Response del Estado de Resultados (sub-issue #271).
 *
 * <p>Reporte del excedente/déficit del período. Solo cuentas de
 * INGRESO (4) y EGRESO (5). El resultado neto (ingresos − egresos) es el
 * "Excedente del Ejercicio" que también aparece en el Balance General.</p>
 */
@Builder
public record EstadoResultadosResponse(
        Encabezado encabezado,
        Seccion ingresos,
        Seccion egresos,
        BigDecimal excedente,           // ingresos.total - egresos.total
        String excedenteEtiqueta        // "EXCEDENTE" / "DÉFICIT" / "—"
) {

    @Builder
    public record Encabezado(
            String razonSocial,
            String rif,
            LocalDate desde,
            LocalDate hasta,
            Instant generadoEn,
            UUID generadoPorUsuarioId,
            boolean incluyeCeros
    ) {}

    /** Sección INGRESOS o EGRESOS con su árbol jerárquico y total. */
    @Builder
    public record Seccion(
            TipoCuentaContable tipo,
            String titulo,
            List<NodoCuenta> rubros,
            BigDecimal total
    ) {}

    /**
     * Nodo jerárquico de cuenta para el Estado de Resultados.
     * <p>Reutiliza el mismo concepto que {@code BalanceGeneralResponse.NodoCuenta}
     * pero se mantiene como tipo distinto para no acoplar los dos reportes.</p>
     */
    @Builder
    public record NodoCuenta(
            String codigo,
            String nombre,
            int nivel,
            NaturalezaSaldo naturaleza,
            BigDecimal saldoNeto,
            BigDecimal saldoPresentacion,
            List<NodoCuenta> hijos
    ) {}
}
