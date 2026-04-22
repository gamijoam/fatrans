// com.tufondo.documentospdf.application.usecase.GenerarTablaAmortizacionUseCase
package com.tufondo.documentospdf.application.usecase;

import com.tufondo.documentospdf.application.dto.DocumentoResponseDTO;
import com.tufondo.core.port.CreditoQueryPort;
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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Caso de uso para generar tablas de amortización.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GenerarTablaAmortizacionUseCase {

    private final PdfGeneratorPort pdfGeneratorPort;
    private final StoragePort storagePort;
    private final MalwareScannerPort malwareScannerPort;
    private final CreditoQueryPort creditoQueryPort;
    private final SocioQueryPort socioQueryPort;
    private final DocumentoRepository documentoRepository;

    private static final String BUCKET = "bucket-creditos";
    private static final int PRESIGNED_URL_MINUTES = 15;

    public DocumentoResponseDTO ejecutar(UUID creditoId, UUID socioIdToken, boolean isAdmin) {
        log.info("Generando tabla de amortización para creditoId={}, socioIdToken={}, isAdmin={}",
                creditoId, socioIdToken, isAdmin);

        // 1. Obtener datos del crédito
        Map<String, Object> datosCredito = creditoQueryPort.obtenerDatosCredito(creditoId);
        if (datosCredito == null) {
            throw new DocumentoNoEncontradoException("Crédito no encontrado: " + creditoId);
        }

        UUID socioId = creditoQueryPort.obtenerSocioIdPorCredito(creditoId);

        // 2. Validación IDOR
        if (!isAdmin && !socioId.equals(socioIdToken)) {
            log.warn("Violación IDOR: socioIdToken={} intentó acceder a tabla de credito de socioId={}",
                    socioIdToken, socioId);
            throw new AccesoNoAutorizadoException("DOC_007");
        }

        // 3. Obtener datos del socio
        Map<String, Object> datosSocio = socioQueryPort.obtenerDatosSocioParaPdf(socioId);

        // 4. Obtener tabla de amortización
        List<Map<String, Object>> tablaAmortizacion = creditoQueryPort.obtenerTablaAmortizacion(creditoId);

        // 5. Preparar datos para el PDF
        Map<String, Object> datosPdf = new HashMap<>();
        datosPdf.put("credito", datosCredito);
        datosPdf.put("socio", datosSocio);
        datosPdf.put("tablaAmortizacion", tablaAmortizacion);
        datosPdf.put("fechaEmision", LocalDate.now().format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy")));

        // 6. Generar PDF
        byte[] pdfBytes;
        try {
            pdfBytes = pdfGeneratorPort.generarPdf(TipoDocumento.TABLA_AMORTIZACION, datosPdf);
        } catch (Exception e) {
            log.error("Error al generar PDF de tabla de amortizacion", e);
            throw new GeneracionPDFException("Error al generar tabla de amortización: " + e.getMessage());
        }

        // 7. Escanear con ClamAV ANTES de calcular hash (seguridad CS-004)
        MalwareScannerPort.ScanResult scanResult = malwareScannerPort.scan(pdfBytes);
        if (scanResult.isMalicious()) {
            log.error("PDF detectado como malicioso: {}", scanResult.threatName());
            throw new EscaneoMalwareException("PDF detectado como malicioso: " + scanResult.threatName());
        }

        // 8. Calcular hash SHA-256
        String hash = calcularHashSha256(pdfBytes);

        // 9. Subir a MinIO
        String nombreArchivo = String.format("TablaAmortizacion_%s.pdf", creditoId);
        String ruta = String.format("tablas-amortizacion/%s/%s", socioId, nombreArchivo);

        StoragePort.UploadResult uploadResult = storagePort.upload(BUCKET, ruta, pdfBytes, "application/pdf");

        // 10. Crear y guardar documento
        Documento documento = Documento.crear(
                socioId,
                TipoDocumento.TABLA_AMORTIZACION,
                nombreArchivo,
                ruta,
                "SHA-256:" + hash,
                uploadResult.tamanoBytes(),
                isAdmin ? "ADMIN" : socioIdToken.toString(),
                ClasificacionDocumento.CONFIDENCIAL
        ).marcarComoAlmacenado();

        documento = documentoRepository.guardar(documento);

        // 11. Generar pre-signed URL
        String preSignedUrl = storagePort.generatePresignedUrl(BUCKET, ruta, PRESIGNED_URL_MINUTES);

        log.info("Tabla de amortización generada exitosamente: documentoId={}", documento.getId());

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
