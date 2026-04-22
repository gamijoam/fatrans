// com.tufondo.documentospdf.infrastructure.security.PdfSecurityValidator
package com.tufondo.documentospdf.infrastructure.security;

import com.tufondo.documentospdf.domain.model.enums.TipoDocumento;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

/**
 * Validador de seguridad para PDFs.
 * Implementa validaciones de seguridad requeridas por OWASP.
 */
@Slf4j
@Component
public class PdfSecurityValidator {

    // Tipos de documento que requieren firma digital
    private static final Set<TipoDocumento> REQUIERE_FIRMA = Set.of(
            TipoDocumento.CONTRATO_ADHESION,
            TipoDocumento.PAGARE
    );

    // Tipos de documento que requieren watermark robusto
    private static final Set<TipoDocumento> REQUIERE_WATERMARK = Set.of(
            TipoDocumento.ESTADO_CUENTA,
            TipoDocumento.CONTRATO_ADHESION,
            TipoDocumento.PAGARE,
            TipoDocumento.TABLA_AMORTIZACION,
            TipoDocumento.CARTA_BENEFICIARIOS
    );

    /**
     * Valida que el PDF tenga el hash SHA-256 calculado correctamente.
     */
    public boolean validarHash(byte[] pdfBytes, String hashEsperado) {
        try {
            String hashCalculado = "SHA-256:" + calcularSha256(pdfBytes);
            boolean valido = hashCalculado.equals(hashEsperado);

            if (!valido) {
                log.warn("Hash SHA-256 no coincide: esperado={}, calculado={}",
                        hashEsperado, hashCalculado);
            }

            return valido;
        } catch (Exception e) {
            log.error("Error al validar hash", e);
            return false;
        }
    }

    /**
     * Calcula el hash SHA-256 de los datos.
     */
    public String calcularSha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /**
     * Verifica si el tipo de documento requiere firma digital.
     */
    public boolean requiereFirmaDigital(TipoDocumento tipo) {
        return REQUIERE_FIRMA.contains(tipo);
    }

    /**
     * Verifica si el tipo de documento requiere watermark.
     */
    public boolean requiereWatermark(TipoDocumento tipo) {
        return REQUIERE_WATERMARK.contains(tipo);
    }

    /**
     * Valida que el PDF tenga firma digital si es requerido.
     */
    public boolean validarFirmaDigital(byte[] pdfBytes, String firmaDigital) {
        if (!requiereFirmaDigital(determinarTipoDocumento(pdfBytes))) {
            return true; // No requiere firma
        }

        if (firmaDigital == null || firmaDigital.isEmpty()) {
            log.warn("Documento requiere firma digital pero no tiene");
            return false;
        }

        // Validar formato de firma RSA-SHA256
        return firmaDigital.startsWith("RSA-SHA256:");
    }

    /**
     * Determina el tipo de documento basándose en el contenido.
     * En una implementación real, esto podría usar metadatos del PDF.
     */
    private TipoDocumento determinarTipoDocumento(byte[] pdfBytes) {
        // Implementación básica - en producción podría leer metadatos
        String contenido = new String(pdfBytes, 0, Math.min(100, pdfBytes.length), StandardCharsets.UTF_8);

        if (contenido.contains("ESTADO DE CUENTA")) return TipoDocumento.ESTADO_CUENTA;
        if (contenido.contains("CONSTANCIA")) return TipoDocumento.CONSTANCIA_AFILIACION;
        if (contenido.contains("CONTRATO")) return TipoDocumento.CONTRATO_ADHESION;
        if (contenido.contains("PAGARÉ")) return TipoDocumento.PAGARE;
        if (contenido.contains("TABLA DE AMORTIZACIÓN")) return TipoDocumento.TABLA_AMORTIZACION;
        if (contenido.contains("BENEFICIARIOS")) return TipoDocumento.CARTA_BENEFICIARIOS;

        return TipoDocumento.ESTADO_CUENTA; // Default
    }

    /**
     * Convierte bytes a representación hexadecimal.
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
