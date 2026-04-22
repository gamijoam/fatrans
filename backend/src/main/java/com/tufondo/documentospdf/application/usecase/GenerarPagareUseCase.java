// com.tufondo.documentospdf.application.usecase.GenerarPagareUseCase
package com.tufondo.documentospdf.application.usecase;

import com.tufondo.documentospdf.application.dto.DocumentoResponseDTO;
import com.tufondo.core.port.CreditoQueryPort;
import com.tufondo.core.port.SocioQueryPort;
import com.tufondo.documentospdf.application.port.MalwareScannerPort;
import com.tufondo.documentospdf.application.port.PdfGeneratorPort;
import com.tufondo.documentospdf.application.port.StoragePort;
import com.tufondo.documentospdf.domain.exception.DocumentoNoEncontradoException;
import com.tufondo.documentospdf.domain.exception.EscaneoMalwareException;
import com.tufondo.documentospdf.domain.exception.FirmaDigitalException;
import com.tufondo.documentospdf.domain.exception.GeneracionPDFException;
import com.tufondo.documentospdf.domain.model.Documento;
import com.tufondo.documentospdf.domain.model.enums.ClasificacionDocumento;
import com.tufondo.documentospdf.domain.model.enums.TipoDocumento;
import com.tufondo.documentospdf.domain.repository.DocumentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Caso de uso para generar pagarés de crédito.
 * Incluye firma digital RSA SHA-256.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GenerarPagareUseCase {

    private final PdfGeneratorPort pdfGeneratorPort;
    private final StoragePort storagePort;
    private final MalwareScannerPort malwareScannerPort;
    private final CreditoQueryPort creditoQueryPort;
    private final SocioQueryPort socioQueryPort;
    private final DocumentoRepository documentoRepository;

    @Value("${documentospdf.firma-digital.clave-privada:#{null}}")
    private String clavePrivadaBase64;

    private static final String BUCKET = "bucket-pagares";
    private static final int PRESIGNED_URL_MINUTES = 15;

    public DocumentoResponseDTO ejecutar(UUID creditoId) {
        log.info("Generando pagaré para creditoId={}", creditoId);

        // 1. Obtener datos del crédito
        Map<String, Object> datosCredito = creditoQueryPort.obtenerDatosCredito(creditoId);
        if (datosCredito == null) {
            throw new DocumentoNoEncontradoException("Crédito no encontrado: " + creditoId);
        }

        UUID socioId = creditoQueryPort.obtenerSocioIdPorCredito(creditoId);

        // 2. Obtener datos del socio
        Map<String, Object> datosSocio = socioQueryPort.obtenerDatosSocioParaPdf(socioId);

        // 3. Obtener tabla de amortización
        var tablaAmortizacion = creditoQueryPort.obtenerTablaAmortizacion(creditoId);

        // 4. Preparar datos para el PDF
        Map<String, Object> datosPdf = new HashMap<>();
        datosPdf.put("credito", datosCredito);
        datosPdf.put("socio", datosSocio);
        datosPdf.put("tablaAmortizacion", tablaAmortizacion);
        datosPdf.put("fechaEmision", LocalDate.now().format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy")));

        // 5. Generar PDF
        byte[] pdfBytes;
        try {
            pdfBytes = pdfGeneratorPort.generarPdf(TipoDocumento.PAGARE, datosPdf);
        } catch (Exception e) {
            log.error("Error al generar PDF de pagaré", e);
            throw new GeneracionPDFException("Error al generar pagaré: " + e.getMessage());
        }

        // 6. Escanear con ClamAV ANTES de firmar (seguridad: no firmar contenido malicioso)
        MalwareScannerPort.ScanResult scanResult = malwareScannerPort.scan(pdfBytes);
        if (scanResult.isMalicious()) {
            log.error("PDF detectado como malicioso: {}", scanResult.threatName());
            throw new EscaneoMalwareException("PDF detectado como malicioso: " + scanResult.threatName());
        }

        // 7. Calcular hash SHA-256
        String hash = calcularHashSha256(pdfBytes);

        // 8. FIRMA DIGITAL RSA SHA-256 (requisito de seguridad CS-001)
        String firmaDigital = firmarPdf(pdfBytes);

        // 9. Subir a MinIO (bucket de pagarés)
        String nombreArchivo = String.format("Pagare_%s.pdf", creditoId);
        String ruta = String.format("pagares/%s/%s", socioId, nombreArchivo);

        StoragePort.UploadResult uploadResult = storagePort.upload(BUCKET, ruta, pdfBytes, "application/pdf");

        // 10. Crear y guardar documento con firma digital
        Documento documento = Documento.crear(
                socioId,
                TipoDocumento.PAGARE,
                nombreArchivo,
                ruta,
                "SHA-256:" + hash,
                uploadResult.tamanoBytes(),
                "SYSTEM",
                ClasificacionDocumento.RESTRINGIDO
        ).marcarComoAlmacenado().conFirmaDigital("RSA-SHA256:" + firmaDigital);

        documento = documentoRepository.guardar(documento);

        // 11. Generar pre-signed URL
        String preSignedUrl = storagePort.generatePresignedUrl(BUCKET, ruta, PRESIGNED_URL_MINUTES);

        log.info("Pagaré generado exitosamente: documentoId={}", documento.getId());

        return DocumentoResponseDTO.builder()
                .documentoId(documento.getId())
                .socioId(documento.getSocioId())
                .tipo(documento.getTipo())
                .nombreArchivo(documento.getNombreArchivo())
                .estado(documento.getEstado())
                .tamanoBytes(documento.getTamanoBytes())
                .hashArchivo(documento.getHashArchivo())
                .clasificacion(documento.getClasificacion())
                .firmaDigital(documento.getFirmaDigital())
                .preSignedUrl(preSignedUrl)
                .urlExpiraEn(PRESIGNED_URL_MINUTES * 60)
                .fechaGeneracion(documento.getFechaGeneracion())
                .fechaExpiracion(documento.getFechaExpiracion())
                .build();
    }

    private String calcularHashSha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private String firmarPdf(byte[] pdfBytes) {
        try {
            if (clavePrivadaBase64 == null || clavePrivadaBase64.isEmpty()) {
                log.error("Error al firmar PDF: configuracion de seguridad incompleta");
                throw new FirmaDigitalException(
                        "DOC_005: No se puede generar PAGARE. Contacte al administrador.");
            }

            byte[] clavePrivadaBytes = Base64.getDecoder().decode(clavePrivadaBase64);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(clavePrivadaBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(pdfBytes);
            byte[] firmaBytes = signature.sign();

            return Base64.getEncoder().encodeToString(firmaBytes);
        } catch (FirmaDigitalException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al firmar digitalmente el PDF", e);
            throw new FirmaDigitalException("Error al firmar el pagaré: " + e.getMessage(), e);
        }
    }
}
