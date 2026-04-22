// com.tufondo.documentospdf.application.usecase.ListarDocumentosSocioUseCase
package com.tufondo.documentospdf.application.usecase;

import com.tufondo.documentospdf.application.dto.DocumentoListResponseDTO;
import com.tufondo.documentospdf.domain.exception.AccesoNoAutorizadoException;
import com.tufondo.documentospdf.domain.model.Documento;
import com.tufondo.documentospdf.domain.model.enums.EstadoDocumento;
import com.tufondo.documentospdf.domain.model.enums.TipoDocumento;
import com.tufondo.documentospdf.domain.repository.DocumentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Caso de uso para listar documentos de un socio.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ListarDocumentosSocioUseCase {

    private final DocumentoRepository documentoRepository;

    public List<DocumentoListResponseDTO> ejecutar(UUID socioId, UUID socioIdToken, boolean isAdmin,
                                                     TipoDocumento tipo, EstadoDocumento estado,
                                                     int page, int size) {
        log.info("Listando documentos para socioId={}, socioIdToken={}, isAdmin={}, tipo={}, estado={}",
                socioId, socioIdToken, isAdmin, tipo, estado);

        // 1. Validación IDOR
        if (!isAdmin && !socioId.equals(socioIdToken)) {
            log.warn("Violación IDOR: socioIdToken={} intentó listar documentos de socioId={}",
                    socioIdToken, socioId);
            throw new AccesoNoAutorizadoException("DOC_007");
        }

        // 2. Obtener documentos según filtros
        List<Documento> documentos;
        long total;

        if (tipo != null && estado != null) {
            documentos = documentoRepository.listarPorSocioYTipo(socioId, tipo, page, size);
            total = documentoRepository.contarPorSocioYTipo(socioId, tipo);
        } else if (tipo != null) {
            documentos = documentoRepository.listarPorSocioYTipo(socioId, tipo, page, size);
            total = documentoRepository.contarPorSocioYTipo(socioId, tipo);
        } else if (estado != null) {
            documentos = documentoRepository.listarPorSocioYEstado(socioId, estado, page, size);
            total = documentoRepository.contarPorSocioYEstado(socioId, estado);
        } else {
            documentos = documentoRepository.listarPorSocio(socioId, page, size);
            total = documentoRepository.contarPorSocio(socioId);
        }

        // 3. Mapear a DTOs
        return documentos.stream()
                .map(doc -> DocumentoListResponseDTO.builder()
                        .documentoId(doc.getId())
                        .tipo(doc.getTipo())
                        .nombreArchivo(doc.getNombreArchivo())
                        .estado(doc.getEstado())
                        .clasificacion(doc.getClasificacion())
                        .fechaGeneracion(doc.getFechaGeneracion())
                        .build())
                .collect(Collectors.toList());
    }
}
