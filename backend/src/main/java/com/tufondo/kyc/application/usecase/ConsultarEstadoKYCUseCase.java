// com.tufondo.kyc.application.usecase.ConsultarEstadoKYCUseCase
package com.tufondo.kyc.application.usecase;

import com.tufondo.kyc.application.dto.response.EstadoKYCResponse;
import com.tufondo.kyc.domain.model.DocumentoIdentidad;
import com.tufondo.kyc.domain.model.VerificacionKYC;
import com.tufondo.kyc.domain.repository.DocumentoIdentidadRepository;
import com.tufondo.kyc.domain.repository.VerificacionKYCRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Use case para consultar el estado del KYC.
 */
@Service
@RequiredArgsConstructor
public class ConsultarEstadoKYCUseCase {

    private final VerificacionKYCRepository verificacionRepository;
    private final DocumentoIdentidadRepository documentoRepository;

    public EstadoKYCResponse ejecutar(UUID socioId) {
        VerificacionKYC verificacion = verificacionRepository.findActiveBySocioId(socioId)
            .orElseThrow(() -> new com.tufondo.kyc.domain.exception.KYCException("No se encontro KYC activo para este socio"));

        List<DocumentoIdentidad> documentos = documentoRepository.findByVerificacionId(verificacion.getId());

        List<EstadoKYCResponse.DocumentoEstadoResponse> docs = documentos.stream()
            .map(doc -> EstadoKYCResponse.DocumentoEstadoResponse.builder()
                .id(doc.getId())
                .tipo(doc.getTipoDocumento())
                .descripcion(doc.getTipoDocumento().getDescripcion())
                .estado(doc.getEstado())
                .nombreOriginal(doc.getNombreOriginal())
                .fechaSubida(doc.getFechaSubida())
                .motivoRechazo(doc.getMotivoRechazo())
                .build())
            .collect(Collectors.toList());

        int diasRestantes = calcularDiasRestantes(verificacion);

        return EstadoKYCResponse.builder()
            .verificacionId(verificacion.getId())
            .socioId(verificacion.getSocioId())
            .nivel(verificacion.getNivel())
            .estado(verificacion.getEstado())
            .descripcionEstado(verificacion.getEstado().getDescripcion())
            .fechaInicio(verificacion.getFechaInicio())
            .fechaExpiracion(verificacion.getFechaExpiracion())
            .diasRestantes(diasRestantes)
            .documentosRequeridos(verificacion.getNivel().getCantidadDocumentosRequeridos())
            .documentosValidos((int) documentos.stream().filter(DocumentoIdentidad::estaValido).count())
            .documentos(docs)
            .comentarioRevision(verificacion.getComentariosRevision())
            .motivoRechazo(verificacion.getMotivoRechazo())
            .build();
    }

    private int calcularDiasRestantes(VerificacionKYC verificacion) {
        if (verificacion.getFechaExpiracion() == null) {
            return -1;
        }
        LocalDateTime ahora = LocalDateTime.now();
        if (verificacion.getFechaExpiracion().isBefore(ahora)) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(ahora, verificacion.getFechaExpiracion());
    }
}