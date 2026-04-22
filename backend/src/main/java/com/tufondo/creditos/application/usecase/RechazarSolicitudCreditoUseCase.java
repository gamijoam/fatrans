// com/tufondo/creditos/application/usecase/RechazarSolicitudCreditoUseCase.java
package com.tufondo.creditos.application.usecase;

import com.tufondo.creditos.application.dto.AprobarRechazarRequest;
import com.tufondo.creditos.domain.exception.CreditoNoEncontradoException;
import com.tufondo.creditos.domain.exception.EstadoCreditoInvalidoException;
import com.tufondo.creditos.domain.model.SolicitudCredito;
import com.tufondo.creditos.domain.model.enums.EstadoSolicitud;
import com.tufondo.creditos.domain.repository.SolicitudCreditoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Caso de uso para rechazar una solicitud de crédito.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RechazarSolicitudCreditoUseCase {

    private final SolicitudCreditoRepository solicitudRepository;

    @Transactional
    public Map<String, Object> ejecutar(String numeroSolicitud, AprobarRechazarRequest request) {
        SolicitudCredito solicitud = solicitudRepository.buscarPorNumeroSolicitud(numeroSolicitud)
            .orElseThrow(() -> new CreditoNoEncontradoException(numeroSolicitud));

        // Validar estado
        if (solicitud.getEstado() != EstadoSolicitud.EN_EVALUACION) {
            throw new EstadoCreditoInvalidoException(numeroSolicitud, solicitud.getEstado(), "rechazo");
        }

        // Transicionar estado
        String motivo = request.getMotivoRechazo() != null ? request.getMotivoRechazo() : "Rechazado sin motivo especificado";
        solicitud.setNotas(motivo);
        solicitud.transicionarA(EstadoSolicitud.RECHAZADA);
        solicitud.setUpdatedAt(LocalDateTime.now());
        solicitudRepository.guardar(solicitud);

        log.info("Solicitud {} rechazada - Motivo: {}", numeroSolicitud, motivo);

        Map<String, Object> response = new HashMap<>();
        response.put("id", solicitud.getId().toString());
        response.put("numeroSolicitud", numeroSolicitud);
        response.put("estado", EstadoSolicitud.RECHAZADA.name());
        response.put("motivoRechazo", motivo);
        response.put("fechaRechazo", LocalDateTime.now().toString());
        return response;
    }
}