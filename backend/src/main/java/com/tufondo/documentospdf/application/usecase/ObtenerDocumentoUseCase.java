// com.tufondo.documentospdf.application.usecase.ObtenerDocumentoUseCase
package com.tufondo.documentospdf.application.usecase;

import com.tufondo.documentospdf.application.dto.DocumentoResponseDTO;
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
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Caso de uso para obtener metadata de un documento.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ObtenerDocumentoUseCase {

    private final DocumentoRepository documentoRepository;

    public DocumentoResponseDTO ejecutar(UUID documentoId, UUID socioIdToken, boolean isAdmin) {
        log.info("Obteniendo metadata de documento: documentoId={}, socioIdToken={}, isAdmin={}",
                documentoId, socioIdToken, isAdmin);

        // 1. Obtener documento
        Documento documento = documentoRepository.buscarPorId(documentoId)
                .orElseThrow(() -> new DocumentoNoEncontradoException("Documento no encontrado: " + documentoId));

        // 2. Validar estado del documento
        if (documento.getEstado() == EstadoDocumento.EXPIRADO) {
            throw new DocumentoExpiradoException("DOC_002: El documento ha expirado");
        }

        if (documento.getEstado() == EstadoDocumento.REVOCADO) {
            throw new DocumentoRevocadoException("DOC_003: El documento ha sido revocado");
        }

        // 3. Validación IDOR
        if (!isAdmin && !documento.getSocioId().equals(socioIdToken)) {
            log.warn("Violación IDOR: socioIdToken={} intentó acceder a documento de socioId={}",
                    socioIdToken, documento.getSocioId());
            throw new AccesoNoAutorizadoException("DOC_007");
        }

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
                .fechaGeneracion(documento.getFechaGeneracion())
                .fechaExpiracion(documento.getFechaExpiracion())
                .build();
    }
}
