// com/tufondo/creditos/application/usecase/ListarSolicitudesPorSocioUseCase.java
package com.tufondo.creditos.application.usecase;

import com.tufondo.creditos.application.dto.SolicitudCreditoResponse;
import com.tufondo.creditos.application.mapper.CreditosDTOMapper;
import com.tufondo.creditos.domain.exception.AccesoNoAutorizadoException;
import com.tufondo.creditos.domain.model.SolicitudCredito;
import com.tufondo.creditos.domain.repository.SolicitudCreditoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Caso de uso para listar solicitudes de crédito por socio.
 * Implementa validación IDOR.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ListarSolicitudesPorSocioUseCase {

    private final SolicitudCreditoRepository solicitudRepository;
    private final CreditosDTOMapper mapper;

    @Transactional(readOnly = true)
    public List<SolicitudCreditoResponse> ejecutar(UUID socioId, UUID socioIdToken, boolean isAdmin) {
        // Validación IDOR: socio solo puede ver sus propias solicitudes
        if (!isAdmin && !socioId.equals(socioIdToken)) {
            throw new AccesoNoAutorizadoException();
        }

        List<SolicitudCredito> solicitudes = solicitudRepository.listarPorSocioId(socioId);
        
        log.info("Listadas {} solicitudes para socioId: {}", solicitudes.size(), socioId);
        return solicitudes.stream()
            .map(mapper::toResponse)
            .collect(Collectors.toList());
    }
}