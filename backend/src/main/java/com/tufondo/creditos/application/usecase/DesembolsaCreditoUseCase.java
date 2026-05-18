// com/tufondo/creditos/application/usecase/DesembolsaCreditoUseCase.java
package com.tufondo.creditos.application.usecase;

import com.tufondo.creditos.application.dto.DesembolsaRequest;
import com.tufondo.creditos.application.mapper.CreditosDTOMapper;
import com.tufondo.creditos.domain.exception.CreditoNoEncontradoException;
import com.tufondo.creditos.domain.exception.EstadoCreditoInvalidoException;
import com.tufondo.creditos.domain.model.PlanAmortizacion;
import com.tufondo.creditos.domain.model.SolicitudCredito;
import com.tufondo.creditos.domain.model.TipoCredito;
import com.tufondo.creditos.domain.model.enums.EstadoSolicitud;
import com.tufondo.creditos.domain.repository.PlanAmortizacionRepository;
import com.tufondo.creditos.domain.repository.SolicitudCreditoRepository;
import com.tufondo.creditos.domain.repository.TipoCreditoRepository;
import com.tufondo.notificaciones.application.service.NotificacionPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Caso de uso para desembolsar un crédito aprobado.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DesembolsaCreditoUseCase {

    private final SolicitudCreditoRepository solicitudRepository;
    private final TipoCreditoRepository tipoCreditoRepository;
    private final PlanAmortizacionRepository planRepository;
    private final CreditosDTOMapper mapper;
    // Issue #214 PR-C
    private final NotificacionPublisher notificacionPublisher;

    @Transactional
    public Map<String, Object> ejecutar(String numeroSolicitud, DesembolsaRequest request, String ipOrigen) {
        SolicitudCredito solicitud = solicitudRepository.buscarPorNumeroSolicitud(numeroSolicitud)
            .orElseThrow(() -> new CreditoNoEncontradoException(numeroSolicitud));

        // Validar estado
        if (solicitud.getEstado() != EstadoSolicitud.APROBADA) {
            throw new EstadoCreditoInvalidoException(numeroSolicitud, solicitud.getEstado(), "desembolso");
        }

        // Obtener tipo de crédito para calcular comisión
        TipoCredito tipoCredito = tipoCreditoRepository.buscarPorId(solicitud.getTipoCreditoId())
            .orElseThrow(() -> new CreditoNoEncontradoException(solicitud.getTipoCreditoId()));

        // Calcular comisión de apertura
        BigDecimal comisionApertura = tipoCredito.getComisionApertura() != null 
            ? solicitud.getMontoSolicitado().multiply(tipoCredito.getComisionApertura())
            : BigDecimal.ZERO;
        
        if (request.getComisionAperturaAplicada() != null) {
            comisionApertura = request.getComisionAperturaAplicada();
        }

        // Calcular monto neto a desembolsar
        BigDecimal montoNeto = solicitud.getMontoSolicitado().subtract(comisionApertura);

        // Generar plan de amortización (sistema francés)
        PlanAmortizacion plan = PlanAmortizacion.crearPlanFrances(
            UUID.randomUUID(),
            solicitud.getId(),
            montoNeto,
            solicitud.getTasaInteresAplicada(),
            solicitud.getPlazoMeses(),
            LocalDate.now().plusDays(1)
        );

        planRepository.guardar(plan);

        // Transicionar solicitud a DESEMBOLSADO
        solicitud.setPlanAmortizacionId(plan.getId());
        solicitud.setReferenciaDesembolso(request.getReferenciaDesembolso());
        solicitud.transicionarA(EstadoSolicitud.DESEMBOLSADO);
        solicitud.setUpdatedAt(LocalDateTime.now());
        solicitudRepository.guardar(solicitud);

        log.info("Crédito desembolsado: {} - Monto: {} - Comision: {} - IP: {}",
            numeroSolicitud, montoNeto, comisionApertura, ipOrigen);

        // Issue #214 PR-C: notificar al socio del desembolso
        notificacionPublisher.notificarSocioCreditoDesembolsado(
                solicitud.getSocioId(), montoNeto, "Bs", numeroSolicitud);

        Map<String, Object> response = new HashMap<>();
        response.put("id", solicitud.getId().toString());
        response.put("numeroSolicitud", numeroSolicitud);
        response.put("estado", EstadoSolicitud.DESEMBOLSADO.name());
        response.put("referenciaDesembolso", request.getReferenciaDesembolso());
        response.put("montoDesembolsado", montoNeto);
        response.put("comisionApertura", comisionApertura);
        response.put("fechaDesembolso", LocalDateTime.now().toString());
        response.put("mensaje", "Desembolso ejecutado exitosamente a cuenta " + solicitud.getCuentaDestino());
        return response;
    }
}