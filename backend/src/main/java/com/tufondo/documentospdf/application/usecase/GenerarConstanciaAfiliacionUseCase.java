// com.tufondo.documentospdf.application.usecase.GenerarConstanciaAfiliacionUseCase
package com.tufondo.documentospdf.application.usecase;

import com.tufondo.documentospdf.application.dto.DocumentoResponseDTO;
import com.tufondo.core.port.SocioQueryPort;
import com.tufondo.documentospdf.application.port.MalwareScannerPort;
import com.tufondo.documentospdf.application.port.PdfGeneratorPort;
import com.tufondo.documentospdf.application.port.StoragePort;
import com.tufondo.documentospdf.domain.exception.AccesoNoAutorizadoException;
import com.tufondo.documentospdf.domain.exception.DocumentoNoEncontradoException;
import com.tufondo.documentospdf.domain.exception.EscaneoMalwareException;
import com.tufondo.documentospdf.domain.exception.GeneracionPDFException;
import com.tufondo.documentospdf.domain.model.Documento;
import com.tufondo.documentospdf.domain.model.enums.ClasificacionDocumento;
import com.tufondo.documentospdf.domain.model.enums.TipoDocumento;
import com.tufondo.documentospdf.domain.repository.DocumentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Caso de uso para generar constancias de afiliación.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GenerarConstanciaAfiliacionUseCase {

    private final PdfGeneratorPort pdfGeneratorPort;
    private final StoragePort storagePort;
    private final MalwareScannerPort malwareScannerPort;
    private final SocioQueryPort socioQueryPort;
    private final DocumentoRepository documentoRepository;

    private static final String BUCKET = "bucket-documentos";
    private static final int PRESIGNED_URL_MINUTES = 15;

    public DocumentoResponseDTO ejecutar(UUID socioId, UUID socioIdToken, boolean isAdmin) {
        log.info("Generando constancia de afiliación para socioId={}, socioIdToken={}, isAdmin={}",
                socioId, socioIdToken, isAdmin);

        // 1. Validación IDOR
        if (!isAdmin && !socioId.equals(socioIdToken)) {
            log.warn("Violación IDOR: socioIdToken={} intentó acceder a constancia de socioId={}",
                    socioIdToken, socioId);
            throw new AccesoNoAutorizadoException("DOC_007");
        }

        // 2. Verificar que el socio existe
        if (!socioQueryPort.existeSocio(socioId)) {
            throw new DocumentoNoEncontradoException("Socio no encontrado: " + socioId);
        }

        // 3. Obtener datos del socio para el PDF
        Map<String, Object> datosSocio = socioQueryPort.obtenerDatosSocioParaPdf(socioId);

        // 4. Preparar datos para el PDF
        Map<String, Object> datosPdf = new HashMap<>();
        datosPdf.put("socio", datosSocio);
        datosPdf.put("fechaEmision", LocalDate.now().format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy")));

        // 5. Generar PDF
        byte[] pdfBytes;
        try {
            pdfBytes = pdfGeneratorPort.generarPdf(TipoDocumento.CONSTANCIA_AFILIACION, datosPdf);
        } catch (Exception e) {
            log.error("Error al generar PDF de constancia de afiliacion", e);
            throw new GeneracionPDFException("Error al generar constancia: " + e.getMessage());
        }

        // 6. Escanear con ClamAV ANTES de calcular hash (seguridad CS-004)
        MalwareScannerPort.ScanResult scanResult = malwareScannerPort.scan(pdfBytes);
        if (scanResult.isMalicious()) {
            log.error("PDF detectado como malicioso: {}", scanResult.threatName());
            throw new EscaneoMalwareException("PDF detectado como malicioso: " + scanResult.threatName());
        }

        // 7. Calcular hash SHA-256
        String hash = calcularHashSha256(pdfBytes);

        // 8. Subir a MinIO
        String nombreArchivo = String.format("ConstanciaAfiliacion_%s.pdf", socioId);
        String ruta = String.format("constancias/%s/%s", socioId, nombreArchivo);

        StoragePort.UploadResult uploadResult = storagePort.upload(BUCKET, ruta, pdfBytes, "application/pdf");

        // 9. Crear y guardar documento
        Documento documento = Documento.crear(
                socioId,
                TipoDocumento.CONSTANCIA_AFILIACION,
                nombreArchivo,
                ruta,
                "SHA-256:" + hash,
                uploadResult.tamanoBytes(),
                socioIdToken.toString(),
                ClasificacionDocumento.PUBLICO
        ).marcarComoAlmacenado();

        documento = documentoRepository.guardar(documento);

        // 10. Generar pre-signed URL
        String preSignedUrl = storagePort.generatePresignedUrl(BUCKET, ruta, PRESIGNED_URL_MINUTES);

        log.info("Constancia de afiliación generada exitosamente: documentoId={}", documento.getId());

        return DocumentoResponseDTO.builder()
                .documentoId(documento.getId())
                .socioId(documento.getSocioId())
                .tipo(documento.getTipo())
                .nombreArchivo(documento.getNombreArchivo())
                .estado(documento.getEstado())
                .tamanoBytes(documento.getTamanoBytes())
                .hashArchivo(documento.getHashArchivo())
                .clasificacion(documento.getClasificacion())
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
}
