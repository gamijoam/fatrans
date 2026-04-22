// com.tufondo.kyc.application.usecase.EnviarDocumentosValidacionUseCase
package com.tufondo.kyc.application.usecase;

import com.tufondo.kyc.application.dto.request.EnviarDocumentosRequest;
import com.tufondo.kyc.application.dto.response.EnviarDocumentosResponse;
import com.tufondo.kyc.domain.model.DocumentoIdentidad;
import com.tufondo.kyc.domain.model.VerificacionKYC;
import com.tufondo.kyc.domain.model.enums.EstadoVerificacion;
import com.tufondo.kyc.domain.repository.DocumentoIdentidadRepository;
import com.tufondo.kyc.domain.repository.VerificacionKYCRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Use case para enviar documentos a validacion.
 */
@Service
@RequiredArgsConstructor
public class EnviarDocumentosValidacionUseCase {

    private final VerificacionKYCRepository verificacionRepository;
    private final DocumentoIdentidadRepository documentoRepository;

    public EnviarDocumentosResponse ejecutar(EnviarDocumentosRequest request, UUID socioId) {

        // 1. Obtener verificacion
        VerificacionKYC verificacion = verificacionRepository.findById(request.getVerificacionId())
            .orElseThrow(() -> new com.tufondo.kyc.domain.exception.VerificacionNotFoundException(request.getVerificacionId()));

        // 2. Validar que es el dueno
        if (!verificacion.getSocioId().equals(socioId)) {
            throw new com.tufondo.kyc.domain.exception.AccesoNoAutorizadoException(
                "No tiene acceso a esta verificacion");
        }

        // 3. Validar estado editable
        if (!verificacion.esEditable()) {
            throw new com.tufondo.kyc.domain.exception.VerificacionNoEditableException(
                "La verificacion no puede enviarse en estado " + verificacion.getEstado());
        }

        // 4. Obtener documentos
        List<DocumentoIdentidad> documentos = documentoRepository.findByVerificacionId(request.getVerificacionId());

        // 5. Validar documentos completos segun nivel
        int docsRequeridos = verificacion.getNivel().getCantidadDocumentosRequeridos();
        if (documentos.size() < docsRequeridos) {
            throw new com.tufondo.kyc.domain.exception.DocumentosIncompletosException(
                "Faltan documentos. Requeridos: " + docsRequeridos + ", Subidos: " + documentos.size());
        }

        // 6. Validar que todos estan Pendiente (no rechazados sin reenvio)
        boolean todosPendientes = documentos.stream()
            .allMatch(DocumentoIdentidad::estaPendiente);

        if (!todosPendientes) {
            throw new com.tufondo.kyc.domain.exception.DocumentosIncompletosException(
                "Hay documentos rechazados que deben ser corregidos antes de enviar");
        }

        // 7. Cambiar estado a EN_REVISION
        verificacion.setEstado(EstadoVerificacion.EN_REVISION);
        verificacion.setUpdatedAt(LocalDateTime.now());
        verificacionRepository.save(verificacion);

        return EnviarDocumentosResponse.builder()
            .verificacionId(verificacion.getId())
            .estado(verificacion.getEstado())
            .documentosEnviados(documentos.size())
            .mensaje("Documentos enviados para revision. Se le notificara cuando esten listos.")
            .build();
    }
}