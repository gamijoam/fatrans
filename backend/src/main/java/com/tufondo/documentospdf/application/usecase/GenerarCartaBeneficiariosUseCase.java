// com.tufondo.documentospdf.application.usecase.GenerarCartaBeneficiariosUseCase
package com.tufondo.documentospdf.application.usecase;

import com.tufondo.documentospdf.application.dto.DocumentoResponseDTO;
import com.tufondo.core.port.BeneficiarioQueryPort;
import com.tufondo.core.port.SocioQueryPort;
import com.tufondo.documentospdf.application.port.MalwareScannerPort;
import com.tufondo.documentospdf.application.port.PdfGeneratorPort;
import com.tufondo.documentospdf.application.port.StoragePort;
import com.tufondo.documentospdf.domain.exception.AccesoNoAutorizadoException;
import com.tufondo.documentospdf.domain.exception.DocumentoNoEncontradoException;
import com.tufondo.documentospdf.domain.exception.EscaneoMalwareException;
import com.tufondo.documentospdf.domain.exception.GeneracionPDFException;
import com.tufondo.documentospdf.domain.exception.TipoDocumentoInvalidoException;
import com.tufondo.documentospdf.domain.model.Documento;
import com.tufondo.documentospdf.domain.model.enums.ClasificacionDocumento;
import com.tufondo.documentospdf.domain.model.enums.TipoDocumento;
import com.tufondo.documentospdf.domain.repository.DocumentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Caso de uso para generar cartas de beneficiarios.
 * Unificado desde el módulo Beneficiarios.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GenerarCartaBeneficiariosUseCase {

    private final PdfGeneratorPort pdfGeneratorPort;
    private final StoragePort storagePort;
    private final MalwareScannerPort malwareScannerPort;
    private final SocioQueryPort socioQueryPort;
    private final BeneficiarioQueryPort beneficiarioQueryPort;
    private final DocumentoRepository documentoRepository;

    private static final String BUCKET = "bucket-documentos";
    private static final int PRESIGNED_URL_MINUTES = 15;

    public DocumentoResponseDTO ejecutar(UUID socioId, UUID socioIdToken, boolean isAdmin) {
        log.info("Generando carta de beneficiarios para socioId={}, socioIdToken={}, isAdmin={}",
                socioId, socioIdToken, isAdmin);

        // 1. Validación IDOR
        if (!isAdmin && !socioId.equals(socioIdToken)) {
            log.warn("Violación IDOR: socioIdToken={} intentó acceder a carta de beneficiario de socioId={}",
                    socioIdToken, socioId);
            throw new AccesoNoAutorizadoException("DOC_007");
        }

        // 2. Verificar que el socio existe
        if (!socioQueryPort.existeSocio(socioId)) {
            throw new DocumentoNoEncontradoException("Socio no encontrado: " + socioId);
        }

        // 3. Obtener datos del socio
        Map<String, Object> datosSocio = socioQueryPort.obtenerDatosSocioParaPdf(socioId);

        // 4. Obtener beneficiarios activos
        List<Map<String, Object>> beneficiarios = beneficiarioQueryPort.obtenerBeneficiariosActivos(socioId);
        if (beneficiarios == null || beneficiarios.isEmpty()) {
            throw new TipoDocumentoInvalidoException("DOC_008: El socio no tiene beneficiarios activos");
        }

        // 5. Validar que la suma de porcentajes sea 100%
        BigDecimal sumaPorcentajes = BigDecimal.ZERO;
        for (Map<String, Object> beneficiario : beneficiarios) {
            BigDecimal porcentaje = (BigDecimal) beneficiario.get("porcentaje");
            sumaPorcentajes = sumaPorcentajes.add(porcentaje);
        }
        if (sumaPorcentajes.compareTo(BigDecimal.valueOf(100)) != 0) {
            throw new TipoDocumentoInvalidoException(
                    "DOC_008: Los porcentajes de beneficiarios deben sumar 100%. Suma actual: " + sumaPorcentajes);
        }

        // 6. Preparar datos para el PDF
        Map<String, Object> datosPdf = new HashMap<>();
        datosPdf.put("socio", datosSocio);
        datosPdf.put("beneficiarios", beneficiarios);
        datosPdf.put("fechaEmision", LocalDate.now().format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy")));

        // 7. Generar PDF
        byte[] pdfBytes;
        try {
            pdfBytes = pdfGeneratorPort.generarPdf(TipoDocumento.CARTA_BENEFICIARIOS, datosPdf);
        } catch (Exception e) {
            log.error("Error al generar PDF de carta de beneficiarios", e);
            throw new GeneracionPDFException("Error al generar carta de beneficiarios: " + e.getMessage());
        }

        // 8. Escanear con ClamAV ANTES de calcular hash (seguridad CS-004)
        MalwareScannerPort.ScanResult scanResult = malwareScannerPort.scan(pdfBytes);
        if (scanResult.isMalicious()) {
            log.error("PDF detectado como malicioso: {}", scanResult.threatName());
            throw new EscaneoMalwareException("PDF detectado como malicioso: " + scanResult.threatName());
        }

        // 9. Calcular hash SHA-256
        String hash = calcularHashSha256(pdfBytes);

        // 10. Subir a MinIO
        String nombreArchivo = String.format("CartaBeneficiarios_%s.pdf", socioId);
        String ruta = String.format("cartas-beneficiarios/%s/%s", socioId, nombreArchivo);

        StoragePort.UploadResult uploadResult = storagePort.upload(BUCKET, ruta, pdfBytes, "application/pdf");

        // 11. Crear y guardar documento
        Documento documento = Documento.crear(
                socioId,
                TipoDocumento.CARTA_BENEFICIARIOS,
                nombreArchivo,
                ruta,
                "SHA-256:" + hash,
                uploadResult.tamanoBytes(),
                isAdmin ? "ADMIN" : socioIdToken.toString(),
                ClasificacionDocumento.CONFIDENCIAL
        ).marcarComoAlmacenado();

        documento = documentoRepository.guardar(documento);

        // 12. Generar pre-signed URL
        String preSignedUrl = storagePort.generatePresignedUrl(BUCKET, ruta, PRESIGNED_URL_MINUTES);

        log.info("Carta de beneficiarios generada exitosamente: documentoId={}", documento.getId());

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
