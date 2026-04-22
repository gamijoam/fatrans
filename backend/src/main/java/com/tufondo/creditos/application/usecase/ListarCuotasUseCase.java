// com/tufondo/creditos/application/usecase/ListarCuotasUseCase.java
package com.tufondo.creditos.application.usecase;

import com.tufondo.creditos.application.dto.CuotaResponse;
import com.tufondo.creditos.application.mapper.CreditosDTOMapper;
import com.tufondo.creditos.domain.exception.AccesoNoAutorizadoException;
import com.tufondo.creditos.domain.exception.CreditoNoEncontradoException;
import com.tufondo.creditos.domain.model.Amortizacion;
import com.tufondo.creditos.domain.model.PlanAmortizacion;
import com.tufondo.creditos.domain.model.SolicitudCredito;
import com.tufondo.creditos.domain.model.enums.EstadoAmortizacion;
import com.tufondo.creditos.domain.repository.AmortizacionRepository;
import com.tufondo.creditos.domain.repository.PlanAmortizacionRepository;
import com.tufondo.creditos.domain.repository.SolicitudCreditoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Caso de uso para listar cuotas de un plan de amortización.
 * Implementa validación IDOR.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ListarCuotasUseCase {

    private final AmortizacionRepository amortizacionRepository;
    private final PlanAmortizacionRepository planRepository;
    private final SolicitudCreditoRepository solicitudRepository;
    private final CreditosDTOMapper mapper;

    @Transactional(readOnly = true)
    public Map<String, Object> ejecutar(String numeroSolicitud, UUID socioIdToken, boolean isAdmin,
            int page, int size, EstadoAmortizacion estado) {
        SolicitudCredito solicitud = solicitudRepository.buscarPorNumeroSolicitud(numeroSolicitud)
            .orElseThrow(() -> new CreditoNoEncontradoException(numeroSolicitud));

        // Validación IDOR
        if (!isAdmin && !solicitud.getSocioId().equals(socioIdToken)) {
            throw new AccesoNoAutorizadoException();
        }

        PlanAmortizacion plan = planRepository.buscarPorSolicitudId(solicitud.getId())
            .orElseThrow(() -> new CreditoNoEncontradoException("Plan no encontrado"));

        List<Amortizacion> cuotas = amortizacionRepository.listarPorPlanId(plan.getId());
        
        // Filtrar por estado si se especifica
        if (estado != null) {
            cuotas = cuotas.stream()
                .filter(c -> c.getEstado() == estado)
                .collect(Collectors.toList());
        }

        // Paginar manualmente
        int start = page * size;
        int end = Math.min(start + size, cuotas.size());
        List<Amortizacion> cuotasPaginadas = start < cuotas.size() 
            ? cuotas.subList(start, end) 
            : List.of();

        List<CuotaResponse> cuotasResponse = cuotasPaginadas.stream()
            .map(mapper::toResponse)
            .collect(Collectors.toList());

        log.info("Listadas {} cuotas para plan {} (page: {}, size: {})", 
            cuotasResponse.size(), plan.getId(), page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("numeroSolicitud", numeroSolicitud);
        response.put("planId", plan.getId().toString());
        response.put("pagina", page);
        response.put("tamanio", size);
        response.put("totalElementos", cuotas.size());
        response.put("cuotas", cuotasResponse);
        return response;
    }
}