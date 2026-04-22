// com/tufondo/creditos/application/usecase/ObtenerPlanAmortizacionUseCase.java
package com.tufondo.creditos.application.usecase;

import com.tufondo.creditos.application.dto.PlanAmortizacionResponse;
import com.tufondo.creditos.application.mapper.CreditosDTOMapper;
import com.tufondo.creditos.domain.exception.AccesoNoAutorizadoException;
import com.tufondo.creditos.domain.exception.CreditoNoEncontradoException;
import com.tufondo.creditos.domain.model.PlanAmortizacion;
import com.tufondo.creditos.domain.model.SolicitudCredito;
import com.tufondo.creditos.domain.repository.PlanAmortizacionRepository;
import com.tufondo.creditos.domain.repository.SolicitudCreditoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso para obtener el plan de amortización de una solicitud.
 * Implementa validación IDOR.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ObtenerPlanAmortizacionUseCase {

    private final PlanAmortizacionRepository planRepository;
    private final SolicitudCreditoRepository solicitudRepository;
    private final CreditosDTOMapper mapper;

    @Transactional(readOnly = true)
    public PlanAmortizacionResponse ejecutar(String numeroSolicitud, UUID socioIdToken, boolean isAdmin) {
        SolicitudCredito solicitud = solicitudRepository.buscarPorNumeroSolicitud(numeroSolicitud)
            .orElseThrow(() -> new CreditoNoEncontradoException(numeroSolicitud));

        // Validación IDOR
        if (!isAdmin && !solicitud.getSocioId().equals(socioIdToken)) {
            throw new AccesoNoAutorizadoException();
        }

        PlanAmortizacion plan = planRepository.buscarPorSolicitudId(solicitud.getId())
            .orElseThrow(() -> new CreditoNoEncontradoException("Plan de amortización no encontrado para " + numeroSolicitud));

        log.info("Plan de amortización consultado: {} por socioId: {}", numeroSolicitud, socioIdToken);
        return mapper.toResponse(plan);
    }
}