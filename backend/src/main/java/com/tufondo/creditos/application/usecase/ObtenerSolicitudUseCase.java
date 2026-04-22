// com/tufondo/creditos/application/usecase/ObtenerSolicitudUseCase.java
package com.tufondo.creditos.application.usecase;

import com.tufondo.creditos.application.dto.SolicitudCreditoResponse;
import com.tufondo.creditos.application.mapper.CreditosDTOMapper;
import com.tufondo.creditos.domain.exception.AccesoNoAutorizadoException;
import com.tufondo.creditos.domain.exception.CreditoNoEncontradoException;
import com.tufondo.creditos.domain.model.SolicitudCredito;
import com.tufondo.creditos.domain.model.TipoCredito;
import com.tufondo.creditos.domain.repository.SolicitudCreditoRepository;
import com.tufondo.creditos.domain.repository.TipoCreditoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso para obtener una solicitud de crédito.
 * Implementa validación IDOR.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ObtenerSolicitudUseCase {

    private final SolicitudCreditoRepository solicitudRepository;
    private final TipoCreditoRepository tipoCreditoRepository;
    private final CreditosDTOMapper mapper;

    @Transactional(readOnly = true)
    public SolicitudCreditoResponse ejecutar(String numeroSolicitud, UUID socioIdToken, boolean isAdmin) {
        SolicitudCredito solicitud = solicitudRepository.buscarPorNumeroSolicitud(numeroSolicitud)
            .orElseThrow(() -> new CreditoNoEncontradoException(numeroSolicitud));

        // Validación IDOR: socio solo puede ver sus propias solicitudes
        if (!isAdmin && !solicitud.getSocioId().equals(socioIdToken)) {
            throw new AccesoNoAutorizadoException();
        }

        TipoCredito tipoCredito = tipoCreditoRepository.buscarPorId(solicitud.getTipoCreditoId()).orElse(null);
        
        SolicitudCreditoResponse response = mapper.toResponse(solicitud);
        if (tipoCredito != null) {
            response.setTipoCreditoNombre(tipoCredito.getNombre());
        }
        
        log.info("Solicitud consultada: {} por socioId: {}", numeroSolicitud, socioIdToken);
        return response;
    }
}