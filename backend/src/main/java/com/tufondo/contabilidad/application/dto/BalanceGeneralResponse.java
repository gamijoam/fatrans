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
 * Response del Balance General (sub-issue #271).
 *
 * <p>Estructura jerárquica: el balance se organiza en dos columnas
 * conceptuales (ACTIVO | PASIVO+PATRIMONIO) cada una con un árbol
 * Rubro → Grupo → Cuenta hoja. El use case ya hace el roll-up de saldos
 * desde las hojas hacia arriba.</p>
 *
 * <p>Decisiones formales en D-008 (ver {@code _decisiones-contables.md}).</p>
 */
@Builder
public record BalanceGeneralResponse(
        Encabezado encabezado,
        Seccion activo,
        Seccion pasivo,
        Seccion patrimonio,
        BigDecimal excedenteEjercicio,    // del Estado de Resultados implícito
        String excedenteEtiqueta,         // "EXCEDENTE" / "DÉFICIT" / "—"
        Totales totales
) {

    @Builder
    public record Encabezado(
            String razonSocial,
            String rif,
            LocalDate fechaCorte,
            LocalDate inicioEjercicio,
            Instant generadoEn,
            UUID generadoPorUsuarioId,
            boolean incluyeCeros
    ) {}

    /**
     * Sección del balance (Activo, Pasivo o Patrimonio). Contiene el árbol
     * jerárquico de cuentas que aplican y el total agregado.
     */
    @Builder
    public record Seccion(
            TipoCuentaContable tipo,
            String titulo,                // "ACTIVO" / "PASIVO" / "PATRIMONIO"
            List<NodoCuenta> rubros,
            BigDecimal total
    ) {}

    /**
     * Nodo del árbol del balance. Las cuentas hoja tienen {@code hijos}
     * vacío. Las cuentas correctoras tienen {@code esCorrectora=true} y
     * se renderizan con signo negativo restando del padre.
     */
    @Builder
    public record NodoCuenta(
            String codigo,
            String nombre,
            int nivel,                    // 1=rubro, 2=grupo, 3=cuenta hoja
            NaturalezaSaldo naturaleza,
            boolean esCorrectora,         // ej. 1.3.99 Provisión Cartera (ACTIVO + ACREEDORA)
            BigDecimal saldoNeto,         // YA con su signo según naturaleza
            BigDecimal saldoPresentacion, // valor absoluto (las correctoras se muestran negativo en PDF)
            List<NodoCuenta> hijos
    ) {}

    /**
     * Validación crítica de cuadre.
     * {@code balanceado=true} ↔ {@code totalActivo = totalPasivo + totalPatrimonio + excedente}.
     */
    @Builder
    public record Totales(
            BigDecimal totalActivo,
            BigDecimal totalPasivo,
            BigDecimal totalPatrimonio,
            BigDecimal excedenteEjercicio,
            BigDecimal totalPasivoMasPatrimonio,    // pasivo + patrimonio + excedente
            BigDecimal diferencia,                   // activo - (pasivo+patrimonio+excedente)
            boolean balanceado
    ) {}
}
