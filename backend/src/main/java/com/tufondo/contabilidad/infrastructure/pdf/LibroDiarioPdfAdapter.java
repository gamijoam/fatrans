package com.tufondo.contabilidad.infrastructure.pdf;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.tufondo.contabilidad.application.dto.LibroDiarioResponse;
import com.tufondo.contabilidad.application.dto.LibroMayorResponse;
import com.tufondo.contabilidad.application.port.output.ContabilidadPdfPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Adapter de generación PDF del Libro Diario (sub-issue #269).
 *
 * <p>Implementa {@link ContabilidadPdfPort} usando la librería OpenPDF, la
 * misma que el módulo {@code documentospdf} (ya está en {@code pom.xml}).</p>
 *
 * <h2>Formato</h2>
 * <ul>
 *   <li>A4 horizontal — para acomodar 7 columnas legibles.</li>
 *   <li>Cabecera con razón social, RIF, período, fecha de generación.</li>
 *   <li>Tabla con columnas: Fecha | Nº Asiento | Origen | Cuenta | Glosa | Debe | Haber.</li>
 *   <li>Cada asiento se renderiza como una fila "agrupadora" (sin DEBE/HABER)
 *       seguida de N filas con las partidas (una por renglón).</li>
 *   <li>Asientos ANULADOS se marcan con fondo rojo claro y la palabra
 *       "ANULADO" en la columna estado.</li>
 *   <li>Totales al pie + folio "Página X de Y" en cada página.</li>
 * </ul>
 */
@Component
@Slf4j
public class LibroDiarioPdfAdapter implements ContabilidadPdfPort {

    private static final Font TITLE_FONT      = new Font(Font.HELVETICA, 14, Font.BOLD, Color.BLACK);
    private static final Font SUBTITLE_FONT   = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.DARK_GRAY);
    private static final Font TABLE_HEADER    = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
    private static final Font ROW_FONT        = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.BLACK);
    private static final Font ROW_FONT_BOLD   = new Font(Font.HELVETICA, 8, Font.BOLD, Color.BLACK);
    private static final Font ROW_FONT_ANUL   = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.RED);
    private static final Font FOOTER_FONT     = new Font(Font.HELVETICA, 7, Font.NORMAL, Color.GRAY);

    private static final Color HEADER_BG          = new Color(70, 70, 70);
    private static final Color ANULADO_BG         = new Color(255, 230, 230);
    private static final Color ASIENTO_HEADER_BG  = new Color(230, 240, 255);

    private static final DateTimeFormatter FECHA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TS_FMT    = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public byte[] generarLibroDiarioPdf(LibroDiarioResponse libro) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4.rotate(), 30, 30, 40, 40);
            PdfWriter writer = PdfWriter.getInstance(doc, baos);
            writer.setPageEvent(new FolioFooter()); // "Página X de Y" en cada página
            doc.open();

            // ─── Encabezado ────────────────────────────────────────────────
            agregarEncabezado(doc, libro.encabezado());

            // ─── Tabla de asientos ─────────────────────────────────────────
            PdfPTable tabla = construirTabla(libro);
            doc.add(tabla);

            // ─── Totales ───────────────────────────────────────────────────
            agregarTotales(doc, libro.totales());

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generando PDF Libro Diario", e);
            throw new RuntimeException("No se pudo generar el Libro Diario: " + e.getMessage(), e);
        }
    }

    // ─── Construcción de secciones ─────────────────────────────────────────

    private void agregarEncabezado(Document doc, LibroDiarioResponse.Encabezado enc) throws DocumentException {
        Paragraph razon = new Paragraph(enc.razonSocial(), TITLE_FONT);
        razon.setAlignment(Element.ALIGN_CENTER);
        doc.add(razon);

        Paragraph rif = new Paragraph("RIF: " + enc.rif(), SUBTITLE_FONT);
        rif.setAlignment(Element.ALIGN_CENTER);
        doc.add(rif);

        Paragraph titulo = new Paragraph("LIBRO DIARIO", TITLE_FONT);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingBefore(8);
        doc.add(titulo);

        String periodo = String.format("Período: %s al %s",
                enc.desde().format(FECHA_FMT), enc.hasta().format(FECHA_FMT));
        Paragraph per = new Paragraph(periodo, SUBTITLE_FONT);
        per.setAlignment(Element.ALIGN_CENTER);
        doc.add(per);

        String gen = String.format("Generado: %s%s",
                enc.generadoEn().atZone(ZoneId.of("America/Caracas")).format(TS_FMT),
                enc.incluyeAnulados() ? " — Incluye asientos anulados" : " — Solo registrados");
        Paragraph genP = new Paragraph(gen, FOOTER_FONT);
        genP.setAlignment(Element.ALIGN_CENTER);
        genP.setSpacingAfter(10);
        doc.add(genP);
    }

    private PdfPTable construirTabla(LibroDiarioResponse libro) throws DocumentException {
        // Ancho relativo de columnas: Fecha | Nº | Origen | Código | Cuenta | Glosa | Debe | Haber
        float[] anchos = {2.2f, 2.2f, 2.2f, 1.5f, 3.5f, 4.5f, 2.0f, 2.0f};
        PdfPTable tabla = new PdfPTable(anchos);
        tabla.setWidthPercentage(100);
        tabla.setHeaderRows(1);

        // Header
        agregarHeaderTabla(tabla,
                "Fecha", "Nº Asiento", "Origen", "Código", "Cuenta", "Glosa", "Debe", "Haber");

        // Filas por asiento
        for (LibroDiarioResponse.AsientoDiario a : libro.asientos()) {
            boolean anulado = a.estado().name().equals("ANULADO");
            Color rowBg = anulado ? ANULADO_BG : Color.WHITE;
            Font rowFont = anulado ? ROW_FONT_ANUL : ROW_FONT;

            // Fila "agrupadora" del asiento (con glosa general y datos de cabecera)
            String tagAnul = anulado ? " [ANULADO]" : "";
            String glosaAgr = (a.glosa() == null ? "" : a.glosa())
                    + (anulado && a.motivoAnulacion() != null
                        ? " — Motivo: " + a.motivoAnulacion() : "");

            agregarCelda(tabla, a.fechaContable().format(FECHA_FMT), rowFont, ASIENTO_HEADER_BG, Element.ALIGN_LEFT);
            agregarCelda(tabla, a.numeroFormateado() + tagAnul, ROW_FONT_BOLD, ASIENTO_HEADER_BG, Element.ALIGN_LEFT);
            agregarCelda(tabla, a.origen().name(), rowFont, ASIENTO_HEADER_BG, Element.ALIGN_LEFT);
            agregarCelda(tabla, "", rowFont, ASIENTO_HEADER_BG, Element.ALIGN_LEFT);
            agregarCelda(tabla, a.referenciaExterna() == null ? "" : a.referenciaExterna(),
                    rowFont, ASIENTO_HEADER_BG, Element.ALIGN_LEFT);
            agregarCelda(tabla, glosaAgr, rowFont, ASIENTO_HEADER_BG, Element.ALIGN_LEFT);
            agregarCelda(tabla, "", rowFont, ASIENTO_HEADER_BG, Element.ALIGN_RIGHT);
            agregarCelda(tabla, "", rowFont, ASIENTO_HEADER_BG, Element.ALIGN_RIGHT);

            // Filas por partida
            for (LibroDiarioResponse.PartidaDiario p : a.partidas()) {
                agregarCelda(tabla, "", rowFont, rowBg, Element.ALIGN_LEFT);
                agregarCelda(tabla, "", rowFont, rowBg, Element.ALIGN_LEFT);
                agregarCelda(tabla, "", rowFont, rowBg, Element.ALIGN_LEFT);
                agregarCelda(tabla, p.codigoCuenta(), rowFont, rowBg, Element.ALIGN_LEFT);
                agregarCelda(tabla, p.nombreCuenta(), rowFont, rowBg, Element.ALIGN_LEFT);
                agregarCelda(tabla, p.glosa() == null ? "" : p.glosa(), rowFont, rowBg, Element.ALIGN_LEFT);
                agregarCelda(tabla, formatoMonto(p.debe()), rowFont, rowBg, Element.ALIGN_RIGHT);
                agregarCelda(tabla, formatoMonto(p.haber()), rowFont, rowBg, Element.ALIGN_RIGHT);
            }
        }

        return tabla;
    }

    private void agregarTotales(Document doc, LibroDiarioResponse.Totales t) throws DocumentException {
        Paragraph espacio = new Paragraph(" ");
        espacio.setSpacingBefore(12);
        doc.add(espacio);

        PdfPTable totales = new PdfPTable(new float[]{14f, 2.0f, 2.0f});
        totales.setWidthPercentage(100);

        String resumen = String.format(
                "TOTALES DEL PERÍODO  —  %d asientos%s  —  %s",
                t.cantidadAsientos(),
                t.cantidadAnulados() > 0 ? " (" + t.cantidadAnulados() + " anulados)" : "",
                t.balanceado() ? "BALANCEADO ✓" : "⚠ DESBALANCEADO");
        Font totalFont = t.balanceado() ? ROW_FONT_BOLD
                : new Font(Font.HELVETICA, 10, Font.BOLD, Color.RED);
        agregarCelda(totales, resumen, totalFont, Color.WHITE, Element.ALIGN_LEFT);
        agregarCelda(totales, formatoMonto(t.totalDebe()), ROW_FONT_BOLD, Color.WHITE, Element.ALIGN_RIGHT);
        agregarCelda(totales, formatoMonto(t.totalHaber()), ROW_FONT_BOLD, Color.WHITE, Element.ALIGN_RIGHT);

        doc.add(totales);
    }

    // ─── Helpers ───────────────────────────────────────────────────────────

    private void agregarHeaderTabla(PdfPTable tabla, String... headers) {
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, TABLE_HEADER));
            cell.setBackgroundColor(HEADER_BG);
            cell.setPadding(4);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabla.addCell(cell);
        }
    }

    private void agregarCelda(PdfPTable tabla, String texto, Font font, Color bg, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(texto == null ? "" : texto, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(3);
        cell.setHorizontalAlignment(align);
        tabla.addCell(cell);
    }

    private String formatoMonto(BigDecimal v) {
        if (v == null || v.signum() == 0) return "";
        return v.toPlainString();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // LIBRO MAYOR (sub-issue #270)
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    public byte[] generarLibroMayorPdf(LibroMayorResponse libro) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4.rotate(), 30, 30, 40, 40);
            PdfWriter writer = PdfWriter.getInstance(doc, baos);
            writer.setPageEvent(new FolioFooterMayor());
            doc.open();

            // Encabezado
            agregarEncabezadoMayor(doc, libro.encabezado());

            // Una sección por cuenta
            for (LibroMayorResponse.CuentaConMovimientos cuenta : libro.cuentas()) {
                agregarSeccionCuenta(doc, cuenta);
            }

            // Totales generales al pie
            agregarTotalesMayor(doc, libro.totales());

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generando PDF Libro Mayor", e);
            throw new RuntimeException("No se pudo generar el Libro Mayor: " + e.getMessage(), e);
        }
    }

    private void agregarEncabezadoMayor(Document doc, LibroMayorResponse.Encabezado enc)
            throws DocumentException {
        Paragraph razon = new Paragraph(enc.razonSocial(), TITLE_FONT);
        razon.setAlignment(Element.ALIGN_CENTER);
        doc.add(razon);

        Paragraph rif = new Paragraph("RIF: " + enc.rif(), SUBTITLE_FONT);
        rif.setAlignment(Element.ALIGN_CENTER);
        doc.add(rif);

        Paragraph titulo = new Paragraph("LIBRO MAYOR", TITLE_FONT);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingBefore(8);
        doc.add(titulo);

        String periodo = String.format("Período: %s al %s",
                enc.desde().format(FECHA_FMT), enc.hasta().format(FECHA_FMT));
        Paragraph per = new Paragraph(periodo, SUBTITLE_FONT);
        per.setAlignment(Element.ALIGN_CENTER);
        doc.add(per);

        StringBuilder sub = new StringBuilder("Generado: ");
        sub.append(enc.generadoEn().atZone(ZoneId.of("America/Caracas")).format(TS_FMT));
        if (enc.filtroCuenta() != null && !enc.filtroCuenta().isBlank()) {
            sub.append(" — Filtrado por cuenta ").append(enc.filtroCuenta());
        }
        Paragraph genP = new Paragraph(sub.toString(), FOOTER_FONT);
        genP.setAlignment(Element.ALIGN_CENTER);
        genP.setSpacingAfter(10);
        doc.add(genP);
    }

    private void agregarSeccionCuenta(Document doc, LibroMayorResponse.CuentaConMovimientos cuenta)
            throws DocumentException {
        // Cabecera de la cuenta
        String cabeceraCuenta = String.format("%s — %s   (%s, naturaleza %s)",
                cuenta.codigo(), cuenta.nombre(), cuenta.tipo(), cuenta.naturaleza());
        Paragraph cab = new Paragraph(cabeceraCuenta,
                new Font(Font.HELVETICA, 10, Font.BOLD, new Color(20, 40, 100)));
        cab.setSpacingBefore(10);
        doc.add(cab);

        // Saldo inicial
        String saldoIni = String.format("Saldo inicial: %s %s",
                formatoMonto(cuenta.saldoInicialNeto()), cuenta.saldoInicialEtiqueta());
        Paragraph sIni = new Paragraph(saldoIni, ROW_FONT_BOLD);
        doc.add(sIni);

        // Tabla de movimientos
        // Cols: Fecha | Nº Asiento | Origen | Contracuenta | Glosa | Debe | Haber | Saldo Acum
        float[] anchos = {1.6f, 1.8f, 1.8f, 3.5f, 4.0f, 1.6f, 1.6f, 1.8f};
        PdfPTable tabla = new PdfPTable(anchos);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(4);
        agregarHeaderTabla(tabla,
                "Fecha", "Nº Asiento", "Origen", "Contracuenta", "Glosa", "Debe", "Haber", "Saldo");

        if (cuenta.movimientos().isEmpty()) {
            // Fila única indicando "sin movimientos en el período"
            PdfPCell vacia = new PdfPCell(new Phrase("(sin movimientos en el período)", ROW_FONT));
            vacia.setColspan(8);
            vacia.setHorizontalAlignment(Element.ALIGN_CENTER);
            vacia.setPadding(6);
            vacia.setBackgroundColor(Color.WHITE);
            tabla.addCell(vacia);
        } else {
            for (LibroMayorResponse.MovimientoMayor m : cuenta.movimientos()) {
                String contracuenta = m.contracuentaCodigo() + " " + m.contracuentaNombre();
                if (m.contracuentaResumen() != null) {
                    contracuenta = contracuenta + " " + m.contracuentaResumen();
                }
                agregarCelda(tabla, m.fechaContable().format(FECHA_FMT), ROW_FONT, Color.WHITE, Element.ALIGN_LEFT);
                agregarCelda(tabla, m.numeroAsientoFormateado(), ROW_FONT, Color.WHITE, Element.ALIGN_LEFT);
                agregarCelda(tabla, m.origen().name(), ROW_FONT, Color.WHITE, Element.ALIGN_LEFT);
                agregarCelda(tabla, contracuenta, ROW_FONT, Color.WHITE, Element.ALIGN_LEFT);
                agregarCelda(tabla, m.glosaAsiento() == null ? "" : m.glosaAsiento(),
                        ROW_FONT, Color.WHITE, Element.ALIGN_LEFT);
                agregarCelda(tabla, formatoMonto(m.debe()), ROW_FONT, Color.WHITE, Element.ALIGN_RIGHT);
                agregarCelda(tabla, formatoMonto(m.haber()), ROW_FONT, Color.WHITE, Element.ALIGN_RIGHT);
                // Saldo acumulado siempre con su signo (puede ser negativo si la cuenta
                // tiene saldo contrario a su naturaleza)
                String saldoStr = m.saldoAcumulado().abs().toPlainString();
                if (m.saldoAcumulado().signum() < 0) saldoStr = "(" + saldoStr + ")";
                agregarCelda(tabla, saldoStr, ROW_FONT_BOLD, Color.WHITE, Element.ALIGN_RIGHT);
            }
        }
        doc.add(tabla);

        // Totales del período de esta cuenta + saldo final
        String resumen = String.format(
                "Movimientos: %d  |  Σ Debe período = %s  |  Σ Haber período = %s",
                cuenta.cantidadMovimientos(),
                formatoMonto(cuenta.totalDebePeriodo()),
                formatoMonto(cuenta.totalHaberPeriodo()));
        Paragraph res = new Paragraph(resumen, ROW_FONT);
        res.setSpacingBefore(2);
        doc.add(res);

        String saldoFin = String.format("Saldo final: %s %s",
                formatoMonto(cuenta.saldoFinalNeto()), cuenta.saldoFinalEtiqueta());
        Paragraph sFin = new Paragraph(saldoFin, ROW_FONT_BOLD);
        sFin.setSpacingAfter(8);
        doc.add(sFin);
    }

    private void agregarTotalesMayor(Document doc, LibroMayorResponse.Totales t) throws DocumentException {
        Paragraph espacio = new Paragraph(" ");
        espacio.setSpacingBefore(12);
        doc.add(espacio);

        PdfPTable totales = new PdfPTable(new float[]{12f, 2.0f, 2.0f});
        totales.setWidthPercentage(100);

        String resumen = String.format(
                "TOTALES GENERALES — %d cuentas | %d movimientos | %s",
                t.cantidadCuentas(), t.cantidadMovimientos(),
                t.balanceado() ? "BALANCEADO ✓" : "⚠ DESBALANCEADO");
        Font totalFont = t.balanceado() ? ROW_FONT_BOLD
                : new Font(Font.HELVETICA, 10, Font.BOLD, Color.RED);
        agregarCelda(totales, resumen, totalFont, Color.WHITE, Element.ALIGN_LEFT);
        agregarCelda(totales, formatoMonto(t.totalDebe()), ROW_FONT_BOLD, Color.WHITE, Element.ALIGN_RIGHT);
        agregarCelda(totales, formatoMonto(t.totalHaber()), ROW_FONT_BOLD, Color.WHITE, Element.ALIGN_RIGHT);
        doc.add(totales);
    }

    /** Footer del Libro Mayor (subclase del de Diario para reutilizar). */
    private static class FolioFooterMayor extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document doc) {
            try {
                PdfContentByte cb = writer.getDirectContent();
                Phrase pieIzq = new Phrase("Libro Mayor — Fatrans", FOOTER_FONT);
                ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, pieIzq,
                        doc.left(), doc.bottom() - 15, 0);
                Phrase pieDer = new Phrase(String.format("Página %d", writer.getPageNumber()), FOOTER_FONT);
                ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, pieDer,
                        doc.right(), doc.bottom() - 15, 0);
            } catch (Exception e) {
                log.warn("Error escribiendo footer del Libro Mayor", e);
            }
        }
    }

    // ─── Folio "Página X de Y" ─────────────────────────────────────────────

    /**
     * Event handler de OpenPDF que pinta el footer con folio "Página X de Y"
     * al pie de cada página. SUDECA exige numeración de páginas continua.
     */
    private static class FolioFooter extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document doc) {
            try {
                PdfContentByte cb = writer.getDirectContent();
                Phrase pieIzq = new Phrase("Libro Diario — Fatrans", FOOTER_FONT);
                ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, pieIzq,
                        doc.left(), doc.bottom() - 15, 0);

                Phrase pieDer = new Phrase(String.format("Página %d", writer.getPageNumber()), FOOTER_FONT);
                ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, pieDer,
                        doc.right(), doc.bottom() - 15, 0);
            } catch (Exception e) {
                // Defensive: el footer es cosmético — no romper la generación si falla.
                log.warn("Error escribiendo footer del Libro Diario", e);
            }
        }
    }
}
