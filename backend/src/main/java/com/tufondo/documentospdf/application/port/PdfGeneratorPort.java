// com.tufondo.documentospdf.application.port.PdfGeneratorPort
package com.tufondo.documentospdf.application.port;

import com.tufondo.documentospdf.domain.model.enums.TipoDocumento;

import java.util.Map;

/**
 * Puerto para generación de documentos PDF.
 * Implementado por OpenPdfGeneratorService.
 */
public interface PdfGeneratorPort {

    /**
     * Genera un PDF según el tipo de documento.
     *
     * @param tipoDocumento tipo de documento a generar
     * @param datos datos necesarios para generar el PDF
     * @return bytes del PDF generado
     */
    byte[] generarPdf(TipoDocumento tipoDocumento, Map<String, Object> datos);
}
