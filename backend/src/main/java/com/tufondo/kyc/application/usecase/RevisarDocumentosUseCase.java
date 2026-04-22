// com.tufondo.kyc.application.usecase.RevisarDocumentosUseCase
package com.tufondo.kyc.application.usecase;

import com.tufondo.kyc.application.dto.request.AprobarVerificacionRequest;
import com.tufondo.kyc.application.dto.request.RechazarVerificacionRequest;
import com.tufondo.kyc.application.dto.request.SolicitarInfoRequest;
import com.tufondo.kyc.application.dto.response.RevisionDecisionResponse;
import com.tufondo.kyc.application.dto.response.RevisionResponse;
import com.tufondo.kyc.domain.model.ConsentimientoKYC;
import com.tufondo.kyc.domain.model.DocumentoIdentidad;
import com.tufondo.kyc.domain.model.VerificacionKYC;
import com.tufondo.kyc.domain.model.enums.EstadoDocumento;
import com.tufondo.kyc.domain.model.enums.EstadoVerificacion;
import com.tufondo.kyc.domain.model.port.StoragePort;
import com.tufondo.kyc.domain.repository.ConsentimientoKYCRepository;
import com.tufondo.kyc.domain.repository.DocumentoIdentidadRepository;
import com.tufondo.kyc.domain.repository.VerificacionKYCRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Use case para que el analista revise documentos.
 */
@Service
@RequiredArgsConstructor
public class RevisarDocumentosUseCase {

    private final VerificacionKYCRepository verificacionRepository;
    private final DocumentoIdentidadRepository documentoRepository;
    private final ConsentimientoKYCRepository consentimientoRepository;
    private final StoragePort storagePort;

    public RevisionResponse obtenerDetalle(UUID verificacionId) {
        VerificacionKYC verificacion = verificacionRepository.findById(verificacionId)
            .orElseThrow(() -> new com.tufondo.kyc.domain.exception.VerificacionNotFoundException(verificacionId));

        // Validar que la verificacion esta en estado de revision (mitigar IDOR)
        if (verificacion.getEstado() != EstadoVerificacion.EN_REVISION) {
            throw new com.tufondo.kyc.domain.exception.AccesoNoAutorizadoException(
                "Verificacion no disponible para revision. Estado actual: " + verificacion.getEstado());
        }

        List<DocumentoIdentidad> documentos = documentoRepository.findByVerificacionId(verificacionId);

        List<RevisionResponse.DocumentoRevisionResponse> docs = documentos.stream()
            .map(doc -> {
                String urlPresignada = storagePort.generatePresignedUrl(doc.getUrlAlmacenamiento(), 15);
                return RevisionResponse.DocumentoRevisionResponse.builder()
                    .id(doc.getId())
                    .tipo(doc.getTipoDocumento())
                    .descripcion(doc.getTipoDocumento().getDescripcion())
                    .estado(doc.getEstado())
                    .urlVisualizacion(urlPresignada)
                    .nombreOriginal(doc.getNombreOriginal())
                    .tamanoBytes(doc.getTamanoBytes())
                    .fechaSubida(doc.getFechaSubida())
                    .metadatosValidacion(doc.getMetadatosValidacion())
                    .build();
            })
            .collect(Collectors.toList());

        // Obtener ultimo consentimiento (sin datos sensibles)
        ConsentimientoKYC consentimiento = consentimientoRepository
            .findLatestBySocioId(verificacion.getSocioId())
            .orElse(null);

        RevisionResponse.ConsentimientoResponse consentimientoResponse = null;
        if (consentimiento != null) {
            consentimientoResponse = RevisionResponse.ConsentimientoResponse.builder()
                .aceptado(consentimiento.isAceptado())
                .fechaConsentimiento(consentimiento.getFechaConsentimiento())
                .build();
        }

        return RevisionResponse.builder()
            .verificacionId(verificacion.getId())
            .socioId(verificacion.getSocioId())
            .nivel(verificacion.getNivel())
            .estado(verificacion.getEstado())
            .fechaInicio(verificacion.getFechaInicio())
            .fechaEnvio(verificacion.getUpdatedAt())
            .documentos(docs)
            .consentimiento(consentimientoResponse)
            .build();
    }

    public RevisionDecisionResponse aprobar(UUID verificacionId, AprobarVerificacionRequest request, String analistaId) {
        VerificacionKYC verificacion = verificacionRepository.findById(verificacionId)
            .orElseThrow(() -> new com.tufondo.kyc.domain.exception.VerificacionNotFoundException(verificacionId));

        if (!verificacion.puedeSerRevisada()) {
            throw new com.tufondo.kyc.domain.exception.VerificacionNoEditableException(
                "La verificacion no esta en estado de revision");
        }

        EstadoVerificacion estadoAnterior = verificacion.getEstado();

        verificacion.setEstado(EstadoVerificacion.APROBADO);
        verificacion.setRevisadoPor(analistaId);
        verificacion.setFechaRevision(LocalDateTime.now());
        verificacion.setComentariosRevision(request.getComentario());
        verificacion.setFechaCompletado(LocalDateTime.now());
        verificacionRepository.save(verificacion);

        // Marcar documentos como validados
        List<DocumentoIdentidad> documentos = documentoRepository.findByVerificacionId(verificacionId);
        for (DocumentoIdentidad doc : documentos) {
            doc.setEstado(EstadoDocumento.VALIDADO);
            documentoRepository.save(doc);
        }

        return RevisionDecisionResponse.builder()
            .verificacionId(verificacion.getId())
            .estadoAnterior(estadoAnterior)
            .estadoNuevo(verificacion.getEstado())
            .mensaje("Su verificacion KYC ha sido aprobada.")
            .build();
    }

    public RevisionDecisionResponse rechazar(UUID verificacionId, RechazarVerificacionRequest request, String analistaId) {
        VerificacionKYC verificacion = verificacionRepository.findById(verificacionId)
            .orElseThrow(() -> new com.tufondo.kyc.domain.exception.VerificacionNotFoundException(verificacionId));

        if (!verificacion.puedeSerRevisada()) {
            throw new com.tufondo.kyc.domain.exception.VerificacionNoEditableException(
                "La verificacion no esta en estado de revision");
        }

        EstadoVerificacion estadoAnterior = verificacion.getEstado();

        verificacion.setEstado(EstadoVerificacion.RECHAZADO);
        verificacion.setRevisadoPor(analistaId);
        verificacion.setFechaRevision(LocalDateTime.now());
        verificacion.setComentariosRevision(request.getComentario());
        verificacion.setMotivoRechazo(request.getComentario());
        verificacionRepository.save(verificacion);

        // Marcar documentos especificos como rechazados si se especificaron
        if (request.getDocumentosRechazados() != null && !request.getDocumentosRechazados().isEmpty()) {
            for (UUID docId : request.getDocumentosRechazados()) {
                documentoRepository.findById(docId).ifPresent(doc -> {
                    doc.setEstado(EstadoDocumento.RECHAZADO);
                    doc.setMotivoRechazo(request.getComentario());
                    documentoRepository.save(doc);
                });
            }
        }

        return RevisionDecisionResponse.builder()
            .verificacionId(verificacion.getId())
            .estadoAnterior(estadoAnterior)
            .estadoNuevo(verificacion.getEstado())
            .mensaje("Su verificacion KYC ha sido rechazada. Revise el motivo e intente nuevamente.")
            .build();
    }

    public RevisionDecisionResponse solicitarInfo(UUID verificacionId, SolicitarInfoRequest request, String analistaId) {
        VerificacionKYC verificacion = verificacionRepository.findById(verificacionId)
            .orElseThrow(() -> new com.tufondo.kyc.domain.exception.VerificacionNotFoundException(verificacionId));

        if (!verificacion.puedeSerRevisada()) {
            throw new com.tufondo.kyc.domain.exception.VerificacionNoEditableException(
                "La verificacion no esta en estado de revision");
        }

        EstadoVerificacion estadoAnterior = verificacion.getEstado();

        verificacion.setEstado(EstadoVerificacion.PENDIENTE);
        verificacion.setRevisadoPor(analistaId);
        verificacion.setFechaRevision(LocalDateTime.now());
        verificacion.setComentariosRevision(request.getComentario());
        verificacionRepository.save(verificacion);

        return RevisionDecisionResponse.builder()
            .verificacionId(verificacion.getId())
            .estadoAnterior(estadoAnterior)
            .estadoNuevo(verificacion.getEstado())
            .mensaje("Se requieren documentos adicionales para completar su verificacion.")
            .build();
    }
}