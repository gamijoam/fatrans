// com/tufondo/creditos/application/mapper/CreditosDTOMapper.java
package com.tufondo.creditos.application.mapper;

import com.tufondo.creditos.application.dto.*;
import com.tufondo.creditos.domain.model.*;
import com.tufondo.creditos.infrastructure.security.XssSanitizer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre entidades de dominio y DTOs.
 */
@Component
public class CreditosDTOMapper {

    private final XssSanitizer xssSanitizer;

    public CreditosDTOMapper(XssSanitizer xssSanitizer) {
        this.xssSanitizer = xssSanitizer;
    }

    // ==================== TipoCredito ====================

    public TipoCreditoResponse toResponse(TipoCredito tipoCredito) {
        if (tipoCredito == null) return null;
        return TipoCreditoResponse.builder()
            .id(tipoCredito.getId())
            .codigo(tipoCredito.getCodigo())
            .nombre(tipoCredito.getNombre())
            .descripcion(xssSanitizer.sanitizeDescription(tipoCredito.getDescripcion()))
            .tasaInteresAnual(tipoCredito.getTasaInteresAnual())
            .plazoMinimoMeses(tipoCredito.getPlazoMinimoMeses())
            .plazoMaximoMeses(tipoCredito.getPlazoMaximoMeses())
            .montoMinimo(tipoCredito.getMontoMinimo())
            .montoMaximo(tipoCredito.getMontoMaximo())
            .porcentajeRequerimientoColateral(tipoCredito.getPorcentajeRequerimientoColateral())
            .comisionApertura(tipoCredito.getComisionApertura())
            .penalidadMoraTasa(tipoCredito.getPenalidadMoraTasa())
            .diasGracia(tipoCredito.getDiasGracia())
            .activo(tipoCredito.getActivo())
            .build();
    }

    public TipoCreditoPublicResponse toPublicResponse(TipoCredito tipoCredito) {
        if (tipoCredito == null) return null;
        return TipoCreditoPublicResponse.builder()
            .id(tipoCredito.getId())
            .codigo(tipoCredito.getCodigo())
            .nombre(tipoCredito.getNombre())
            .descripcion(xssSanitizer.sanitizeDescription(tipoCredito.getDescripcion()))
            .tasaInteresAnual(tipoCredito.getTasaInteresAnual())
            .plazoMinimoMeses(tipoCredito.getPlazoMinimoMeses())
            .plazoMaximoMeses(tipoCredito.getPlazoMaximoMeses())
            .montoMinimo(tipoCredito.getMontoMinimo())
            .montoMaximo(tipoCredito.getMontoMaximo())
            .porcentajeRequerimientoColateral(tipoCredito.getPorcentajeRequerimientoColateral())
            .diasGracia(tipoCredito.getDiasGracia())
            .build();
    }

    // ==================== SolicitudCredito ====================
    
    public SolicitudCreditoResponse toResponse(SolicitudCredito solicitud) {
        if (solicitud == null) return null;
        return SolicitudCreditoResponse.builder()
            .id(solicitud.getId() != null ? solicitud.getId().toString() : null)
            .numeroSolicitud(solicitud.getNumeroSolicitud())
            .socioId(solicitud.getSocioId())
            .tipoCreditoId(solicitud.getTipoCreditoId())
            .montoSolicitado(solicitud.getMontoSolicitado())
            .plazoMeses(solicitud.getPlazoMeses())
            .tasaInteresAplicada(solicitud.getTasaInteresAplicada())
            .cuotaMensualEstimada(solicitud.getCuotaMensualEstimada())
            .estado(solicitud.getEstado() != null ? solicitud.getEstado().name() : null)
            .colateralCuentaId(solicitud.getColateralCuentaId() != null ? solicitud.getColateralCuentaId().toString() : null)
            .colateralMontoRetenido(solicitud.getColateralMontoRetenido())
            .destinoCredito(solicitud.getDestinoCredito())
            .productoFinanciableId(solicitud.getProductoFinanciableId())
            .productoNombreSnapshot(solicitud.getProductoNombreSnapshot())
            .productoPrecioSnapshot(solicitud.getProductoPrecioSnapshot())
            .productoMonedaSnapshot(solicitud.getProductoMonedaSnapshot())
            .productoColateralRequeridoSnapshot(solicitud.getProductoColateralRequeridoSnapshot())
            .createdAt(solicitud.getCreatedAt())
            .build();
    }

    // ==================== EvaluacionCrediticia ====================
    
    public EvaluacionResponse toResponse(EvaluacionCrediticia evaluacion) {
        if (evaluacion == null) return null;
        return EvaluacionResponse.builder()
            .id(evaluacion.getId() != null ? evaluacion.getId().toString() : null)
            .solicitudId(evaluacion.getSolicitudId() != null ? evaluacion.getSolicitudId().toString() : null)
            .socioId(evaluacion.getSocioId())
            .puntajeAntiguedad(evaluacion.getPuntajeAntiguedad())
            .puntajeHistorialAhorro(evaluacion.getPuntajeHistorialAhorro())
            .puntajeCapacidadPago(evaluacion.getPuntajeCapacidadPago())
            .scoreInterno(evaluacion.getScoreInterno())
            .scoreHash(evaluacion.getScoreHash())
            .elegible(evaluacion.getElegible())
            .nivelRiesgo(evaluacion.getNivelRiesgo() != null ? evaluacion.getNivelRiesgo().name() : null)
            .tasaInteresFinal(evaluacion.getTasaInteresFinal())
            .mensajeDecision(evaluacion.getMensajeDecision())
            .evaluador(evaluacion.getEvaluador())
            .build();
    }

    // ==================== PlanAmortizacion ====================
    
    public PlanAmortizacionResponse toResponse(PlanAmortizacion plan) {
        if (plan == null) return null;
        return PlanAmortizacionResponse.builder()
            .id(plan.getId() != null ? plan.getId().toString() : null)
            .solicitudId(plan.getSolicitudId() != null ? plan.getSolicitudId().toString() : null)
            .montoPrincipal(plan.getMontoPrincipal())
            .tasaInteres(plan.getTasaInteres())
            .plazoMeses(plan.getPlazoMeses())
            .frecuenciaPago(plan.getFrecuenciaPago() != null ? plan.getFrecuenciaPago().name() : null)
            .fechaInicio(plan.getFechaInicio())
            .fechaFin(plan.getFechaFin())
            .numeroCuotas(plan.getNumeroCuotas())
            .cuotaMensual(plan.getCuotaMensual())
            .totalIntereses(plan.getTotalIntereses())
            .totalPagado(plan.getTotalPagado())
            .saldoPendiente(plan.getSaldoPendiente())
            .estado(plan.getEstado() != null ? plan.getEstado().name() : null)
            .cuotas(plan.getCuotas() != null ? 
                plan.getCuotas().stream().map(this::toResponse).collect(Collectors.toList()) : null)
            .build();
    }

    // ==================== Amortizacion ====================
    
    public CuotaResponse toResponse(Amortizacion amortizacion) {
        if (amortizacion == null) return null;
        return CuotaResponse.builder()
            .id(amortizacion.getId() != null ? amortizacion.getId().toString() : null)
            .numeroCuota(amortizacion.getNumeroCuota())
            .fechaVencimiento(amortizacion.getFechaVencimiento())
            .fechaPago(amortizacion.getFechaPago())
            .capital(amortizacion.getCapital())
            .interes(amortizacion.getInteres())
            .montoCuota(amortizacion.getMontoCuota())
            .saldoInsoluto(amortizacion.getSaldoInsoluto())
            .estado(amortizacion.getEstado() != null ? amortizacion.getEstado().name() : null)
            .diasMora(amortizacion.getDiasMora())
            .interesMora(amortizacion.getInteresMora())
            .montoPagado(amortizacion.getMontoPagado())
            .build();
    }

    public SimulacionResponse.CuotaSimulada toCuotaSimulada(Amortizacion amortizacion) {
        if (amortizacion == null) return null;
        return SimulacionResponse.CuotaSimulada.builder()
            .numeroCuota(amortizacion.getNumeroCuota())
            .fechaVencimiento(amortizacion.getFechaVencimiento() != null ? amortizacion.getFechaVencimiento().toString() : null)
            .capital(amortizacion.getCapital())
            .interes(amortizacion.getInteres())
            .montoCuota(amortizacion.getMontoCuota())
            .saldoInsoluto(amortizacion.getSaldoInsoluto())
            .build();
    }

    public List<SimulacionResponse.CuotaSimulada> toCuotaSimuladaList(List<Amortizacion> amortizaciones) {
        if (amortizaciones == null) return null;
        return amortizaciones.stream().map(this::toCuotaSimulada).collect(Collectors.toList());
    }
}
