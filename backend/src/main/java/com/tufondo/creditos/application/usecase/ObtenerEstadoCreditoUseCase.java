// com/tufondo/creditos/application/usecase/ObtenerEstadoCreditoUseCase.java
package com.tufondo.creditos.application.usecase;

import com.tufondo.creditos.application.dto.CreditoResponse;
import com.tufondo.creditos.application.dto.PlanAmortizacionResponse;
import com.tufondo.creditos.application.dto.TipoCreditoResponse;
import com.tufondo.creditos.application.mapper.CreditosDTOMapper;
import com.tufondo.creditos.domain.exception.AccesoNoAutorizadoException;
import com.tufondo.creditos.domain.exception.CreditoNoEncontradoException;
import com.tufondo.creditos.domain.model.Amortizacion;
import com.tufondo.creditos.domain.model.PlanAmortizacion;
import com.tufondo.creditos.domain.model.SolicitudCredito;
import com.tufondo.creditos.domain.model.TipoCredito;
import com.tufondo.creditos.domain.model.enums.EstadoAmortizacion;
import com.tufondo.creditos.domain.repository.AmortizacionRepository;
import com.tufondo.creditos.domain.repository.PlanAmortizacionRepository;
import com.tufondo.creditos.domain.repository.SolicitudCreditoRepository;
import com.tufondo.creditos.domain.repository.TipoCreditoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Caso de uso para obtener el estado completo de un crédito.
 * Implementa validación IDOR.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ObtenerEstadoCreditoUseCase {

    private final SolicitudCreditoRepository solicitudRepository;
    private final TipoCreditoRepository tipoCreditoRepository;
    private final PlanAmortizacionRepository planRepository;
    private final AmortizacionRepository amortizacionRepository;
    private final CreditosDTOMapper mapper;

    @Transactional(readOnly = true)
    public CreditoResponse ejecutar(String numeroSolicitud, UUID socioIdToken, boolean isAdmin) {
        SolicitudCredito solicitud = solicitudRepository.buscarPorNumeroSolicitud(numeroSolicitud)
            .orElseThrow(() -> new CreditoNoEncontradoException(numeroSolicitud));

        // Validación IDOR
        if (!isAdmin && !solicitud.getSocioId().equals(socioIdToken)) {
            throw new AccesoNoAutorizadoException();
        }

        TipoCredito tipoCredito = tipoCreditoRepository.buscarPorId(solicitud.getTipoCreditoId()).orElse(null);
        TipoCreditoResponse tipoCreditoResponse = tipoCredito != null ? mapper.toResponse(tipoCredito) : null;
        
        // Construir respuesta
        CreditoResponse.CreditoResponseBuilder responseBuilder = CreditoResponse.builder()
            .id(solicitud.getId().toString())
            .numeroSolicitud(numeroSolicitud)
            .socioId(solicitud.getSocioId())
            .tipoCredito(tipoCreditoResponse)
            .montoSolicitado(solicitud.getMontoSolicitado())
            .plazoMeses(solicitud.getPlazoMeses())
            .tasaInteresAplicada(solicitud.getTasaInteresAplicada())
            .estado(solicitud.getEstado().name())
            .colateralMontoRetenido(solicitud.getColateralMontoRetenido())
            .referenciaDesembolso(solicitud.getReferenciaDesembolso())
            .fechaDesembolso(solicitud.getUpdatedAt());

        // Obtener plan de amortización si existe
        if (solicitud.getPlanAmortizacionId() != null) {
            PlanAmortizacion plan = planRepository.buscarPorId(solicitud.getPlanAmortizacionId()).orElse(null);
            if (plan != null) {
                List<Amortizacion> cuotas = amortizacionRepository.listarPorPlanId(plan.getId());
                
                long cuotasPagadas = cuotas.stream()
                    .filter(c -> c.getEstado() == EstadoAmortizacion.PAGADA)
                    .count();
                long cuotasVencidas = cuotas.stream()
                    .filter(c -> c.estaVencida())
                    .count();
                long cuotasPendientes = cuotas.size() - cuotasPagadas;

                // Calcular totales de intereses pagados
                BigDecimal totalInteresesPagados = cuotas.stream()
                    .filter(c -> c.getEstado() == EstadoAmortizacion.PAGADA)
                    .map(Amortizacion::getInteres)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Encontrar próximo vencimiento (primera cuota pendiente)
                LocalDate proximoVencimiento = cuotas.stream()
                    .filter(c -> c.getEstado() == EstadoAmortizacion.PENDIENTE)
                    .map(Amortizacion::getFechaVencimiento)
                    .min(LocalDate::compareTo)
                    .orElse(null);

                PlanAmortizacionResponse planResponse = mapper.toResponse(plan);
                
                responseBuilder.plan(planResponse);
                
                CreditoResponse.ResumenResponse resumen = CreditoResponse.ResumenResponse.builder()
                    .cuotasPagadas((int) cuotasPagadas)
                    .cuotasPendientes((int) cuotasPendientes)
                    .cuotasVencidas((int) cuotasVencidas)
                    .totalIntereses(plan.getTotalIntereses())
                    .totalPagadoIntereses(totalInteresesPagados)
                    .build();
                responseBuilder.resumen(resumen);
            }
        }

        CreditoResponse response = responseBuilder.build();
        log.info("Estado de crédito consultado: {} por socioId: {}", numeroSolicitud, socioIdToken);
        return response;
    }
}