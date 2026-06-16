package com.tufondo.contabilidad.application.port.output;

import com.tufondo.contabilidad.application.dto.BalanceGeneralResponse;
import com.tufondo.contabilidad.application.dto.EstadoResultadosResponse;
import com.tufondo.contabilidad.application.dto.LibroDiarioResponse;
import com.tufondo.contabilidad.application.dto.LibroMayorResponse;

/**
 * Puerto de salida para generación de reportes contables en PDF.
 *
 * <p>Sub-issue #269 (Libro Diario) y #270 (Libro Mayor). Se crea propio del
 * módulo contabilidad en lugar de reutilizar el {@code PdfGeneratorPort} de
 * {@code documentospdf} porque:</p>
 * <ul>
 *   <li>Aquel está acoplado a {@code TipoDocumento} enum del otro módulo.</li>
 *   <li>Los reportes contables tienen contrato distinto (DTO tipado vs Map).</li>
 *   <li>Mantiene separación de responsabilidades entre módulos.</li>
 * </ul>
 *
 * <p>La implementación PUEDE compartir librería (OpenPDF) — solo el contrato
 * es separado.</p>
 */
public interface ContabilidadPdfPort {

    /**
     * Genera el PDF del Libro Diario a partir del response ya armado por
     * el use case (con encabezado, asientos y totales).
     *
     * @param libroDiario datos completos del reporte
     * @return bytes del PDF listo para descarga
     */
    byte[] generarLibroDiarioPdf(LibroDiarioResponse libroDiario);

    /**
     * Genera el PDF del Libro Mayor: una sección por cuenta con saldo
     * inicial, movimientos del período con contracuenta, saldo acumulado
     * por línea, y saldo final. Totales generales al pie.
     *
     * @param libroMayor datos completos del reporte (sub-issue #270)
     * @return bytes del PDF listo para descarga
     */
    byte[] generarLibroMayorPdf(LibroMayorResponse libroMayor);

    /**
     * Genera el PDF del Balance General (sub-issue #271).
     * Formato vertical clásico VEN-NIF en dos columnas (Activo |
     * Pasivo+Patrimonio) con jerarquía Rubro→Grupo→Cuenta. Excedente
     * del Ejercicio incluido en Patrimonio. Validación visual de cuadre
     * al pie ("BALANCEADO ✓" / "⚠ DESBALANCEADO").
     */
    byte[] generarBalanceGeneralPdf(BalanceGeneralResponse balance);

    /**
     * Genera el PDF del Estado de Resultados (sub-issue #271).
     * Formato columnar: secciones INGRESOS y EGRESOS con sus rubros,
     * grupos y cuentas; al pie el Excedente/Déficit del período.
     */
    byte[] generarEstadoResultadosPdf(EstadoResultadosResponse estadoResultados);
}
