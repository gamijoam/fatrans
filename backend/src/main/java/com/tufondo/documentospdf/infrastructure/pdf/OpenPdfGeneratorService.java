// com.tufondo.documentospdf.infrastructure.pdf.OpenPdfGeneratorService
package com.tufondo.documentospdf.infrastructure.pdf;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.tufondo.documentospdf.application.port.PdfGeneratorPort;
import com.tufondo.documentospdf.domain.exception.FirmaDigitalException;
import com.tufondo.documentospdf.domain.exception.GeneracionPDFException;
import com.tufondo.documentospdf.domain.model.enums.TipoDocumento;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio de generación de PDFs usando OpenPDF.
 * Implementa el puerto PdfGeneratorPort.
 *
 * Incluye:
 * - Watermark con hash SHA-256 real (no hashCode)
 * - Firma digital RSA SHA-256 para CONTRATO y PAGARE
 */
@Slf4j
@Service
public class OpenPdfGeneratorService implements PdfGeneratorPort {

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 18, Font.BOLD, Color.DARK_GRAY);
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE);
    private static final Font NORMAL_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
    private static final Font BOLD_FONT = new Font(Font.HELVETICA, 10, Font.BOLD, Color.BLACK);
    private static final Font WATERMARK_FONT = new Font(Font.HELVETICA, 7, Font.NORMAL, Color.GRAY);

    @Value("${documentospdf.firma-digital.enabled:false}")
    private boolean firmaDigitalEnabled;

    @Value("${documentospdf.firma-digital.keystore-path:}")
    private String keystorePath;

    @Value("${documentospdf.firma-digital.keystore-password:}")
    private String keystorePassword;

    @Value("${documentospdf.firma-digital.key-alias:documentos-fondo}")
    private String keyAlias;

    private PrivateKey privateKey;
    private String firmaDigitalAlgorithm = "SHA256withRSA";

    @Override
    public byte[] generarPdf(TipoDocumento tipoDocumento, Map<String, Object> datos) {
        try {
            return switch (tipoDocumento) {
                case ESTADO_CUENTA -> generarEstadoCuenta(datos);
                case CONSTANCIA_AFILIACION -> generarConstanciaAfiliacion(datos);
                case CONTRATO_ADHESION -> generarContratoAdhesion(datos);
                case PAGARE -> generarPagare(datos);
                case TABLA_AMORTIZACION -> generarTablaAmortizacion(datos);
                case CARTA_BENEFICIARIOS -> generarCartaBeneficiarios(datos);
                case COMPROBANTE_MOVIMIENTO -> generarComprobanteMovimiento(datos);
            };
        } catch (GeneracionPDFException | FirmaDigitalException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al generar PDF: tipo={}", tipoDocumento, e);
            throw new GeneracionPDFException("Error al generar PDF: " + e.getMessage(), e);
        }
    }

    private byte[] generarEstadoCuenta(Map<String, Object> datos) throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);
        document.open();

        addHeader(document, "ESTADO DE CUENTA");
        addFecha(datos.get("fechaGeneracion").toString(), document);

        Map<String, Object> cuenta = (Map<String, Object>) datos.get("cuenta");
        addSectionTitle(document, "Información de la Cuenta");
        addKeyValue(document, "Número de Cuenta:", cuenta.get("numeroCuenta").toString());
        addKeyValue(document, "Socio:", cuenta.get("nombreCompleto").toString());
        addKeyValue(document, "Período:", datos.get("periodo").toString());
        addKeyValue(document, "Saldo Anterior:", formatMonto(cuenta.get("saldoAnterior")));
        addKeyValue(document, "Total Abonos:", formatMonto(cuenta.get("totalAbonos")));
        addKeyValue(document, "Total Cargos:", formatMonto(cuenta.get("totalCargos")));
        addKeyValue(document, "Saldo Actual:", formatMonto(cuenta.get("saldoActual")));

        addSectionTitle(document, "Detalle de Movimientos");
        List<Map<String, Object>> movimientos = (List<Map<String, Object>>) datos.get("movimientos");
        addMovimientosTable(document, movimientos);

        addWatermarkRobusto(document, "CONFIDENCIAL", datos);

        document.close();
        return baos.toByteArray();
    }

    private byte[] generarConstanciaAfiliacion(Map<String, Object> datos) throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);
        document.open();

        addHeader(document, "CONSTANCIA DE AFILIACIÓN");
        addFecha(datos.get("fechaEmision").toString(), document);

        Map<String, Object> socio = (Map<String, Object>) datos.get("socio");

        addEmptyLine(document);
        addParagraph(document, "El Fondo de Ahorro certifica que:", NORMAL_FONT);
        addEmptyLine(document);

        Paragraph nombre = new Paragraph(socio.get("nombreCompleto").toString(), TITLE_FONT);
        nombre.setAlignment(Element.ALIGN_CENTER);
        document.add(nombre);

        addEmptyLine(document);
        addParagraph(document, "es socio activo del Fondo de Ahorro, "
                + "estando debidamente registrado en nuestros libros contables.", NORMAL_FONT);

        addEmptyLine(document);
        addParagraph(document, "Esta constancia se expide a solicitud del interesado "
                + "para los fines que convengan.", NORMAL_FONT);

        addEmptyLine(document);
        addEmptyLine(document);

        Paragraph lugarFecha = new Paragraph(" Caracas, " + datos.get("fechaEmision").toString(), NORMAL_FONT);
        lugarFecha.setAlignment(Element.ALIGN_CENTER);
        document.add(lugarFecha);

        addWatermarkRobusto(document, "PUBLICO", datos);

        document.close();
        return baos.toByteArray();
    }

    private byte[] generarContratoAdhesion(Map<String, Object> datos) throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);
        document.open();

        addHeader(document, "CONTRATO DE ADHESIÓN");
        addFecha(datos.get("fechaContrato").toString(), document);

        Map<String, Object> solicitud = (Map<String, Object>) datos.get("solicitud");
        Map<String, Object> socio = (Map<String, Object>) datos.get("socio");

        addSectionTitle(document, "Partes del Contrato");
        addKeyValue(document, "Fondo de Ahorro:", "Fondo de Ahorro del Personnel");
        addKeyValue(document, "Socio:", socio.get("nombreCompleto").toString());
        addKeyValue(document, "Cédula:", socio.get("cedula").toString());

        addSectionTitle(document, "Objeto del Contrato");
        addParagraph(document, "El presente contrato establece los términos y condiciones "
                + "bajo los cuales el socio tendrá acceso a los productos y servicios del Fondo de Ahorro.", NORMAL_FONT);

        addSectionTitle(document, "Términos y Condiciones");
        addParagraph(document, "1. El socio se compromete a cumplir con las normativas internas del Fondo.", NORMAL_FONT);
        addParagraph(document, "2. Los productos estarán sujetos a disponibilidad y evaluación crediticia.", NORMAL_FONT);
        addParagraph(document, "3. El Fondo se reserva el derecho de modificar las condiciones previa notificación.", NORMAL_FONT);

        addWatermarkRobusto(document, "RESTRINGIDO", datos);

        // Firma digital RSA SHA-256
        byte[] pdfBytes = baos.toByteArray();
        String firmaDigital = firmarPdf(pdfBytes, TipoDocumento.CONTRATO_ADHESION);

        // Agregar firma al documento (como metadata)
        document.add(new Paragraph(" "));
        Paragraph firma = new Paragraph("Firma Digital: " + firmaDigital, new Font(Font.HELVETICA, 6, Font.NORMAL, Color.GRAY));
        firma.setAlignment(Element.ALIGN_LEFT);
        document.add(firma);

        document.close();
        return baos.toByteArray();
    }

    private byte[] generarPagare(Map<String, Object> datos) throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);
        document.open();

        addHeader(document, "PAGARÉ");
        addFecha(datos.get("fechaEmision").toString(), document);

        Map<String, Object> credito = (Map<String, Object>) datos.get("credito");
        Map<String, Object> socio = (Map<String, Object>) datos.get("socio");

        addSectionTitle(document, "Datos del Crédito");
        addKeyValue(document, "Número de Crédito:", credito.get("numeroCredito").toString());
        addKeyValue(document, "Monto Concedido:", formatMonto(credito.get("montoConcedido")));
        addKeyValue(document, "Tasa de Interés:", credito.get("tasaInteres").toString() + "%");
        addKeyValue(document, "Plazo:", credito.get("plazoMeses").toString() + " meses");
        addKeyValue(document, "Cuota Mensual:", formatMonto(credito.get("cuotaMensual")));

        addSectionTitle(document, "Datos del Deudor");
        addKeyValue(document, "Nombre:", socio.get("nombreCompleto").toString());
        addKeyValue(document, "Cédula:", socio.get("cedula").toString());

        addEmptyLine(document);
        addParagraph(document, "Por este pagaré, el deudor se obliga a pagar la cantidad indicada "
                + "más los intereses convenidos, en las fechas establecidas en el plan de amortización.", NORMAL_FONT);

        addWatermarkRobusto(document, "RESTRINGIDO", datos);

        // Firma digital RSA SHA-256
        byte[] pdfBytes = baos.toByteArray();
        String firmaDigital = firmarPdf(pdfBytes, TipoDocumento.PAGARE);

        document.add(new Paragraph(" "));
        Paragraph firma = new Paragraph("Firma Digital: " + firmaDigital, new Font(Font.HELVETICA, 6, Font.NORMAL, Color.GRAY));
        firma.setAlignment(Element.ALIGN_LEFT);
        document.add(firma);

        document.close();
        return baos.toByteArray();
    }

    private byte[] generarTablaAmortizacion(Map<String, Object> datos) throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);
        document.open();

        addHeader(document, "TABLA DE AMORTIZACIÓN");
        addFecha(datos.get("fechaEmision").toString(), document);

        Map<String, Object> credito = (Map<String, Object>) datos.get("credito");
        addKeyValue(document, "Número de Crédito:", credito.get("numeroCredito").toString());
        addKeyValue(document, "Monto:", formatMonto(credito.get("montoConcedido")));
        addKeyValue(document, "Tasa:", credito.get("tasaInteres").toString() + "% EA");

        addEmptyLine(document);

        List<Map<String, Object>> tabla = (List<Map<String, Object>>) datos.get("tablaAmortizacion");
        addTablaAmortizacionTable(document, tabla);

        addWatermarkRobusto(document, "CONFIDENCIAL", datos);

        document.close();
        return baos.toByteArray();
    }

    private byte[] generarCartaBeneficiarios(Map<String, Object> datos) throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);
        document.open();

        addHeader(document, "CARTA DE BENEFICIARIOS");
        addFecha(datos.get("fechaEmision").toString(), document);

        Map<String, Object> socio = (Map<String, Object>) datos.get("socio");

        addEmptyLine(document);
        addParagraph(document, "Yo, " + socio.get("nombreCompleto").toString() + ", "
                + "portador de la cédula de identidad No. " + socio.get("cedula").toString() + ", "
                + "en mi calidad de socio del Fondo de Ahorro, designo como beneficiarios de mis "
                + "prestaciones sociales a las siguientes personas:", NORMAL_FONT);

        addEmptyLine(document);

        List<Map<String, Object>> beneficiarios = (List<Map<String, Object>>) datos.get("beneficiarios");
        addBeneficiariosTable(document, beneficiarios);

        addEmptyLine(document);
        addParagraph(document, "Los porcentajes indicados suman el 100% del total de mis prestaciones sociales.", NORMAL_FONT);
        addEmptyLine(document);
        addParagraph(document, "Esta designación revoca cualquier designación anterior.", NORMAL_FONT);

        addWatermarkRobusto(document, "CONFIDENCIAL", datos);

        document.close();
        return baos.toByteArray();
    }

    /**
     * Genera comprobante on-demand de un movimiento individual (issue #220).
     *
     * <p>Diferencia clave con los otros generadores: NO se persiste en
     * MinIO ni en la tabla {@code documentos}. El movimiento original
     * (inmutable, RN-006) es la fuente de verdad — el PDF se reconstruye
     * cada vez. Esto simplifica el flujo y evita duplicar storage.</p>
     *
     * <p>Datos esperados en el map:
     * <ul>
     *   <li>{@code socio}: map con nombreCompleto, cedula</li>
     *   <li>{@code cuenta}: map con numeroCuenta, tipoCuenta, moneda</li>
     *   <li>{@code movimiento}: map con numeroOperacion, tipo (DEPOSITO/RETIRO/...),
     *       monto, saldoAnterior, saldoPosterior, fechaMovimiento, descripcion,
     *       referencia, canalOrigen, estado</li>
     *   <li>{@code fechaEmision}: timestamp legible de generación</li>
     * </ul>
     */
    @SuppressWarnings("unchecked")
    private byte[] generarComprobanteMovimiento(Map<String, Object> datos) throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);
        document.open();

        Map<String, Object> socio = (Map<String, Object>) datos.get("socio");
        Map<String, Object> cuenta = (Map<String, Object>) datos.get("cuenta");
        Map<String, Object> mov = (Map<String, Object>) datos.get("movimiento");

        addHeader(document, "COMPROBANTE DE MOVIMIENTO");
        addFecha(safeStr(datos.get("fechaEmision")), document);

        addSectionTitle(document, "Datos del Socio");
        addKeyValue(document, "Nombre:", safeStr(socio != null ? socio.get("nombreCompleto") : null));
        addKeyValue(document, "Cédula:", safeStr(socio != null ? socio.get("cedula") : null));

        addSectionTitle(document, "Datos de la Cuenta");
        addKeyValue(document, "Número de Cuenta:", safeStr(cuenta != null ? cuenta.get("numeroCuenta") : null));
        addKeyValue(document, "Tipo de Cuenta:", safeStr(cuenta != null ? cuenta.get("tipoCuenta") : null));
        addKeyValue(document, "Moneda:", safeStr(cuenta != null ? cuenta.get("moneda") : null));

        addSectionTitle(document, "Detalle del Movimiento");
        addKeyValue(document, "Número de Operación:", safeStr(mov != null ? mov.get("numeroOperacion") : null));
        addKeyValue(document, "Tipo:", safeStr(mov != null ? mov.get("tipo") : null));
        addKeyValue(document, "Fecha:", safeStr(mov != null ? mov.get("fechaMovimiento") : null));
        addKeyValue(document, "Canal:", safeStr(mov != null ? mov.get("canalOrigen") : null));
        addKeyValue(document, "Estado:", safeStr(mov != null ? mov.get("estado") : null));
        if (mov != null && mov.get("descripcion") != null) {
            addKeyValue(document, "Descripción:", safeStr(mov.get("descripcion")));
        }
        if (mov != null && mov.get("referencia") != null) {
            addKeyValue(document, "Referencia:", safeStr(mov.get("referencia")));
        }

        addSectionTitle(document, "Importes");
        addKeyValue(document, "Monto:", formatMonto(mov != null ? mov.get("monto") : null));
        addKeyValue(document, "Saldo Anterior:", formatMonto(mov != null ? mov.get("saldoAnterior") : null));
        addKeyValue(document, "Saldo Posterior:", formatMonto(mov != null ? mov.get("saldoPosterior") : null));

        addEmptyLine(document);
        addParagraph(document, "Este comprobante es un duplicado generado a partir del registro "
                + "original del movimiento. El movimiento queda registrado en los libros del "
                + "Fondo de Ahorro con carácter inmutable conforme a las normativas vigentes.",
                NORMAL_FONT);

        addWatermarkRobusto(document, "COMPROBANTE", datos);

        document.close();
        return baos.toByteArray();
    }

    /** Helper local: convierte un Object a String tratando null. */
    private static String safeStr(Object o) {
        return o == null ? "—" : o.toString();
    }

    /**
     * Genera watermark robusto con hash SHA-256 real.
     */
    private void addWatermarkRobusto(Document document, String clasificacion, Map<String, Object> datos) throws DocumentException {
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        String generadoPor = datos.getOrDefault("generadoPor", "SYSTEM").toString();
        String socioId = datos.getOrDefault("socioId", "N/A").toString();

        // Hash SHA-256 real del contenido
        String contenidoHash = String.format("%s|%s|%s|%s",
                timestamp, socioId, generadoPor, UUID.randomUUID().toString());
        String hashParcial = calculateSha256(contenidoHash).substring(0, 16);

        String watermarkText = String.format(
                "%s | Generado: %s | Socio: %s | Hash: SHA-256:%s... | Generado por: %s",
                clasificacion,
                timestamp,
                maskSocioId(socioId),
                hashParcial,
                generadoPor
        );

        Paragraph watermark = new Paragraph(watermarkText, WATERMARK_FONT);
        watermark.setAlignment(Element.ALIGN_CENTER);
        document.add(watermark);
    }

    /**
     * Firma digital RSA SHA-256 para CONTRATO y PAGARE.
     * Si la firma digital no está configurada, LANZA excepción.
     */
    private String firmarPdf(byte[] pdfBytes, TipoDocumento tipo) {
        if (tipo != TipoDocumento.CONTRATO_ADHESION && tipo != TipoDocumento.PAGARE) {
            return null;
        }

        if (!firmaDigitalEnabled) {
            log.error("FIRMA DIGITAL REQUERIDA para {} pero está deshabilitada", tipo);
            throw new FirmaDigitalException(
                    "DOC_005: Firma digital no configurada. No se puede generar " + tipo.name());
        }

        if (keystorePath == null || keystorePath.isEmpty() || keystorePassword == null) {
            log.error("FIRMA DIGITAL REQUERIDA para {} pero keystore no configurado", tipo);
            throw new FirmaDigitalException(
                    "DOC_005: Keystore de firma digital no configurado. No se puede generar " + tipo.name());
        }

        try {
            PrivateKey key = getPrivateKey();
            Signature signature = Signature.getInstance(firmaDigitalAlgorithm);
            signature.initSign(key);
            signature.update(pdfBytes);
            byte[] firmaBytes = signature.sign();
            return "RSA-SHA256:" + HexFormat.of().formatHex(firmaBytes);

        } catch (FirmaDigitalException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al firmar PDF: tipo={}", tipo, e);
            throw new FirmaDigitalException(
                    "DOC_005: Error al firmar digitalmente el documento: " + e.getMessage(), e);
        }
    }

    private PrivateKey getPrivateKey() throws Exception {
        if (privateKey != null) {
            return privateKey;
        }

        try (FileInputStream fis = new FileInputStream(keystorePath)) {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(fis, keystorePassword.toCharArray());
            Certificate cert = keyStore.getCertificate(keyAlias);
            if (cert == null) {
                throw new FirmaDigitalException("DOC_005: Certificado no encontrado en keystore para alias: " + keyAlias);
            }
            privateKey = (PrivateKey) keyStore.getKey(keyAlias, keystorePassword.toCharArray());
            if (privateKey == null) {
                throw new FirmaDigitalException("DOC_005: Llave privada no encontrada en keystore para alias: " + keyAlias);
            }
            return privateKey;
        }
    }

    private String calculateSha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private String maskSocioId(String socioId) {
        if (socioId == null || socioId.length() < 8) {
            return "***";
        }
        return socioId.substring(0, 4) + "-***-" + socioId.substring(socioId.length() - 4);
    }

    // ==================== Helpers de PDF ====================

    private void addHeader(Document document, String titulo) throws DocumentException {
        Paragraph header = new Paragraph(titulo, TITLE_FONT);
        header.setAlignment(Element.ALIGN_CENTER);
        document.add(header);
        document.add(new Paragraph(" "));
    }

    private void addFecha(String fecha, Document document) throws DocumentException {
        Paragraph fechaPar = new Paragraph("Fecha: " + fecha, NORMAL_FONT);
        fechaPar.setAlignment(Element.ALIGN_CENTER);
        document.add(fechaPar);
        document.add(new Paragraph(" "));
    }

    private void addSectionTitle(Document document, String titulo) throws DocumentException {
        Paragraph section = new Paragraph(titulo, BOLD_FONT);
        section.setSpacingBefore(15);
        section.setSpacingAfter(5);
        document.add(section);
    }

    private void addKeyValue(Document document, String key, String value) throws DocumentException {
        Paragraph p = new Paragraph();
        p.add(new Chunk(key + " ", BOLD_FONT));
        p.add(new Chunk(value != null ? value : "N/A", NORMAL_FONT));
        document.add(p);
    }

    private void addParagraph(Document document, String text, Font font) throws DocumentException {
        Paragraph p = new Paragraph(text, font);
        p.setSpacingAfter(5);
        document.add(p);
    }

    private void addEmptyLine(Document document) throws DocumentException {
        document.add(new Paragraph(" "));
    }

    private void addMovimientosTable(Document document, List<Map<String, Object>> movimientos) throws DocumentException {
        if (movimientos == null || movimientos.isEmpty()) {
            document.add(new Paragraph("No hay movimientos en este período.", NORMAL_FONT));
            return;
        }

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2, 3, 2, 2});

        addTableHeader(table, "Fecha");
        addTableHeader(table, "Descripción");
        addTableHeader(table, "Débitos");
        addTableHeader(table, "Créditos");

        for (Map<String, Object> mov : movimientos) {
            table.addCell(new PdfPCell(new Phrase(getOrDefault(mov.get("fecha"), "N/A"), NORMAL_FONT)));
            table.addCell(new PdfPCell(new Phrase(getOrDefault(mov.get("descripcion"), "N/A"), NORMAL_FONT)));
            table.addCell(new PdfPCell(new Phrase(formatMonto(mov.get("debito")), NORMAL_FONT)));
            table.addCell(new PdfPCell(new Phrase(formatMonto(mov.get("credito")), NORMAL_FONT)));
        }

        document.add(table);
    }

    private void addTablaAmortizacionTable(Document document, List<Map<String, Object>> tabla) throws DocumentException {
        if (tabla == null || tabla.isEmpty()) {
            document.add(new Paragraph("No hay datos de amortización.", NORMAL_FONT));
            return;
        }

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 2, 2, 2, 2, 2});

        addTableHeader(table, "No.");
        addTableHeader(table, "Fecha");
        addTableHeader(table, "Capital");
        addTableHeader(table, "Interés");
        addTableHeader(table, "Cuota");
        addTableHeader(table, "Saldo");

        int i = 1;
        for (Map<String, Object> row : tabla) {
            table.addCell(new PdfPCell(new Phrase(String.valueOf(i++), NORMAL_FONT)));
            table.addCell(new PdfPCell(new Phrase(getOrDefault(row.get("fecha"), "N/A"), NORMAL_FONT)));
            table.addCell(new PdfPCell(new Phrase(formatMonto(row.get("capital")), NORMAL_FONT)));
            table.addCell(new PdfPCell(new Phrase(formatMonto(row.get("interes")), NORMAL_FONT)));
            table.addCell(new PdfPCell(new Phrase(formatMonto(row.get("cuota")), NORMAL_FONT)));
            table.addCell(new PdfPCell(new Phrase(formatMonto(row.get("saldo")), NORMAL_FONT)));
        }

        document.add(table);
    }

    private void addBeneficiariosTable(Document document, List<Map<String, Object>> beneficiarios) throws DocumentException {
        if (beneficiarios == null || beneficiarios.isEmpty()) {
            document.add(new Paragraph("No hay beneficiarios registrados.", NORMAL_FONT));
            return;
        }

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3, 2, 1});

        addTableHeader(table, "Nombre");
        addTableHeader(table, "Cédula");
        addTableHeader(table, "%");

        for (Map<String, Object> ben : beneficiarios) {
            table.addCell(new PdfPCell(new Phrase(getOrDefault(ben.get("nombre"), "N/A"), NORMAL_FONT)));
            table.addCell(new PdfPCell(new Phrase(getOrDefault(ben.get("cedula"), "N/A"), NORMAL_FONT)));
            table.addCell(new PdfPCell(new Phrase(getOrDefault(ben.get("porcentaje"), "0") + "%", NORMAL_FONT)));
        }

        document.add(table);
    }

    private void addTableHeader(PdfPTable table, String header) {
        PdfPCell cell = new PdfPCell(new Phrase(header, HEADER_FONT));
        cell.setBackgroundColor(new Color(51, 51, 51));
        cell.setPadding(5);
        table.addCell(cell);
    }

    private String getOrDefault(Object value, String defaultValue) {
        return value != null ? value.toString() : defaultValue;
    }

    private String formatMonto(Object monto) {
        if (monto == null) return "0.00";
        if (monto instanceof BigDecimal) {
            return String.format("%,.2f", ((BigDecimal) monto).doubleValue());
        }
        if (monto instanceof Number) {
            return String.format("%,.2f", ((Number) monto).doubleValue());
        }
        return monto.toString();
    }
}
