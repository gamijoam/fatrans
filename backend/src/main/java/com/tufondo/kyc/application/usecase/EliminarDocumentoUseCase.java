// com.tufondo.kyc.application.usecase.EliminarDocumentoUseCase
package com.tufondo.kyc.application.usecase;

import com.tufondo.kyc.application.dto.response.EliminarDocumentoResponse;
import com.tufondo.kyc.domain.model.DocumentoIdentidad;
import com.tufondo.kyc.domain.model.VerificacionKYC;
import com.tufondo.kyc.domain.model.port.StoragePort;
import com.tufondo.kyc.domain.repository.DocumentoIdentidadRepository;
import com.tufondo.kyc.domain.repository.VerificacionKYCRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Use case para eliminar un documento.
 */
@Service
@RequiredArgsConstructor
public class EliminarDocumentoUseCase {

    private final DocumentoIdentidadRepository documentoRepository;
    private final VerificacionKYCRepository verificacionRepository;
    private final StoragePort storagePort;

    public EliminarDocumentoResponse ejecutar(UUID documentoId, UUID socioId) {

        DocumentoIdentidad documento = documentoRepository.findById(documentoId)
            .orElseThrow(() -> new com.tufondo.kyc.domain.exception.DocumentoNotFoundException(documentoId));

        // 1. Validar que el documento pertenece al socio
        if (!documento.getSocioId().equals(socioId)) {
            throw new com.tufondo.kyc.domain.exception.AccesoNoAutorizadoException(
                "No tiene acceso a este documento");
        }

        // 2. Validar que puede eliminarse (solo Pendiente)
        if (!documento.puedeSerEliminado()) {
            throw new com.tufondo.kyc.domain.exception.DocumentoNoEliminableException(
                "El documento no puede eliminarse en estado " + documento.getEstado());
        }

        // 3. Validar que la verificacion asociada esta editable
        if (documento.getVerificacionId() != null) {
            VerificacionKYC verificacion = verificacionRepository.findById(documento.getVerificacionId())
                .orElse(null);

            if (verificacion != null && !verificacion.esEditable()) {
                throw new com.tufondo.kyc.domain.exception.DocumentoNoEliminableException(
                    "El documento no puede eliminarse porque la verificacion ya fue enviada");
            }
        }

        // 4. Eliminar archivo de storage
        storagePort.delete(documento.getUrlAlmacenamiento());

        // 5. Eliminar registro de la base de datos
        documentoRepository.delete(documentoId);

        return EliminarDocumentoResponse.builder()
            .eliminado(true)
            .mensaje("Documento eliminado exitosamente")
            .build();
    }
}