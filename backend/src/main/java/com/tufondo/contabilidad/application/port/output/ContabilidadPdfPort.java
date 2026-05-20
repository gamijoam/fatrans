package com.tufondo.contabilidad.application.port.output;

import com.tufondo.contabilidad.application.dto.LibroDiarioResponse;

/**
 * Puerto de salida para generación de reportes contables en PDF.
 *
 * <p>Sub-issue #269. Se crea propio del módulo contabilidad en lugar de
 * reutilizar el {@code PdfGeneratorPort} de {@code documentospdf} porque:</p>
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
}
