// com.tufondo.documentospdf.application.usecase.DescargarDocumentoUseCase
package com.tufondo.documentospdf.application.usecase;

import com.tufondo.documentospdf.application.dto.DescargarDocumentoResponseDTO;
import com.tufondo.documentospdf.application.port.StoragePort;
import com.tufondo.documentospdf.domain.exception.AccesoNoAutorizadoException;
import com.tufondo.documentospdf.domain.exception.DocumentoExpiradoException;
import com.tufondo.documentospdf.domain.exception.DocumentoNoEncontradoException;
import com.tufondo.documentospdf.domain.exception.DocumentoRevocadoException;
import com.tufondo.documentospdf.domain.model.Documento;
import com.tufondo.documentospdf.domain.model.enums.EstadoDocumento;
import com.tufondo.documentospdf.domain.repository.DocumentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Caso de uso para descargar documentos.
 * Genera pre-signed URLs para acceso seguro a MinIO.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DescargarDocumentoUseCase {

    private final DocumentoRepository documentoRepository;
    private final StoragePort storagePort;

    @Value("${documentospdf.storage.presigned-url-expiration-minutes:15}")
    private int presignedUrlExpirationMinutes;

    public DescargarDocumentoResponseDTO ejecutar(UUID documentoId, UUID socioIdToken, boolean isAdmin) {
        log.info("Descargando documento: documentoId={}, socioIdToken={}, isAdmin={}",
                documentoId, socioIdToken, isAdmin);

        // 1. Obtener documento
        Documento documento = documentoRepository.buscarPorId(documentoId)
                .orElseThrow(() -> new DocumentoNoEncontradoException("Documento no encontrado: " + documentoId));

        // 2. Validar estado del documento
        if (documento.getEstado() == EstadoDocumento.EXPIRADO) {
            log.warn("Intento de descargar documento expirado: {}", documentoId);
            throw new DocumentoExpiradoException("DOC_002: El documento ha expirado");
        }

        if (documento.getEstado() == EstadoDocumento.REVOCADO) {
            log.warn("Intento de descargar documento revocado: {}", documentoId);
            throw new DocumentoRevocadoException("DOC_003: El documento ha sido revocado");
        }

        if (documento.getEstado() == EstadoDocumento.GENERADO) {
            log.warn("Intento de descargar documento no almacenado: {}", documentoId);
            throw new DocumentoNoEncontradoException("Documento aún en proceso de generación");
        }

        // 3. Validación IDOR: socio solo puede descargar sus propios documentos
        if (!isAdmin && !documento.getSocioId().equals(socioIdToken)) {
            log.warn("Violación IDOR: socioIdToken={} intentó descargar documento de socioId={}",
                    socioIdToken, documento.getSocioId());
            throw new AccesoNoAutorizadoException("DOC_007");
        }

        // 4. Determinar bucket según tipo de documento
        String bucket = obtenerBucketPorTipo(documento);

        // 5. Generar pre-signed URL
        String preSignedUrl = storagePort.generatePresignedUrl(
                bucket,
                documento.getRutaAlmacenamiento(),
                presignedUrlExpirationMinutes
        );

        // 6. Calcular fecha de expiración de la URL
        LocalDateTime urlExpiraEn = LocalDateTime.now().plusMinutes(presignedUrlExpirationMinutes);

        log.info("Pre-signed URL generada para documento: {}", documentoId);

        return DescargarDocumentoResponseDTO.builder()
                .documentoId(documento.getId())
                .preSignedUrl(preSignedUrl)
                .urlExpiraEn(presignedUrlExpirationMinutes * 60)
                .fechaExpiracion(urlExpiraEn)
                .build();
    }

    private String obtenerBucketPorTipo(Documento documento) {
        return switch (documento.getTipo()) {
            case ESTADO_CUENTA, CONSTANCIA_AFILIACION, CARTA_BENEFICIARIOS -> "bucket-documentos";
            case CONTRATO_ADHESION -> "bucket-contratos";
            case PAGARE -> "bucket-pagares";
            case TABLA_AMORTIZACION -> "bucket-creditos";
        };
    }
}
