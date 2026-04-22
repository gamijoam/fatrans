// Adaptador para el puerto CreditoQueryPort usado por el módulo Documentos PDF
package com.tufondo.creditos.infrastructure.adapter;

import com.tufondo.creditos.infrastructure.persistence.entity.AmortizacionEntity;
import com.tufondo.creditos.infrastructure.persistence.entity.PlanAmortizacionEntity;
import com.tufondo.creditos.infrastructure.persistence.entity.SolicitudCreditoEntity;
import com.tufondo.creditos.infrastructure.persistence.jpa.AmortizacionJpaRepository;
import com.tufondo.creditos.infrastructure.persistence.jpa.PlanAmortizacionJpaRepository;
import com.tufondo.creditos.infrastructure.persistence.jpa.SolicitudCreditoJpaRepository;
import com.tufondo.core.port.CreditoQueryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador para el puerto CreditoQueryPort.
 * Provee datos del módulo Créditos para el módulo Documentos PDF.
 *
 * NOTA: Si el crédito no existe, estos métodos lanzarán excepciones
 * para evitar fallos silenciosos.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreditoQueryPortAdapter implements CreditoQueryPort {

    private final SolicitudCreditoJpaRepository solicitudCreditoJpaRepository;
    private final PlanAmortizacionJpaRepository planAmortizacionJpaRepository;
    private final AmortizacionJpaRepository amortizacionJpaRepository;

    @Override
    public Map<String, Object> obtenerDatosCredito(UUID creditoId) {
        log.debug("Obteniendo datos de crédito: creditoId={}", creditoId);

        SolicitudCreditoEntity entity = solicitudCreditoJpaRepository.findById(creditoId)
                .orElseThrow(() -> {
                    log.error("Crédito no encontrado: creditoId={}", creditoId);
                    return new CreditoNoEncontradoException(
                            "DOC_001: Crédito no encontrado: " + creditoId);
                });

        return mapSolicitudEntityToMap(entity);
    }

    @Override
    public List<Map<String, Object>> obtenerTablaAmortizacion(UUID creditoId) {
        log.debug("Obteniendo tabla de amortización para crédito: creditoId={}", creditoId);

        // Verificar que el crédito existe primero
        if (!solicitudCreditoJpaRepository.existsById(creditoId)) {
            log.error("Crédito no encontrado para tabla de amortización: creditoId={}", creditoId);
            throw new CreditoNoEncontradoException(
                    "DOC_001: Crédito no encontrado: " + creditoId);
        }

        Optional<PlanAmortizacionEntity> planOpt = planAmortizacionJpaRepository.findBySolicitudId(creditoId);

        if (planOpt.isEmpty()) {
            log.warn("No se encontró plan de amortización para creditoId={}", creditoId);
            throw new PlanNoEncontradoException(
                    "DOC_001: Plan de amortización no encontrado para crédito: " + creditoId);
        }

        PlanAmortizacionEntity plan = planOpt.get();
        List<AmortizacionEntity> cuotas = amortizacionJpaRepository.findByPlanId(plan.getId());

        log.debug("Tabla de amortización encontrada: {} cuotas para creditoId={}", cuotas.size(), creditoId);

        return cuotas.stream()
                .map(this::mapAmortizacionEntityToMap)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> obtenerDatosSolicitud(UUID solicitudId) {
        log.debug("Obteniendo datos de solicitud: solicitudId={}", solicitudId);

        SolicitudCreditoEntity entity = solicitudCreditoJpaRepository.findById(solicitudId)
                .orElseThrow(() -> {
                    log.error("Solicitud no encontrada: solicitudId={}", solicitudId);
                    return new SolicitudNoEncontradaException(
                            "DOC_001: Solicitud de crédito no encontrada: " + solicitudId);
                });

        return mapSolicitudEntityToMap(entity);
    }

    @Override
    public UUID obtenerSocioIdPorCredito(UUID creditoId) {
        log.debug("Obteniendo socioId para crédito: creditoId={}", creditoId);

        SolicitudCreditoEntity entity = solicitudCreditoJpaRepository.findById(creditoId)
                .orElseThrow(() -> {
                    log.error("Crédito no encontrado: creditoId={}", creditoId);
                    return new CreditoNoEncontradoException(
                            "DOC_001: Crédito no encontrado: " + creditoId);
                });

        log.debug("SocioId encontrado para creditoId={}: socioId={}", creditoId, entity.getSocioId());
        return entity.getSocioId();
    }

    private Map<String, Object> mapSolicitudEntityToMap(SolicitudCreditoEntity entity) {
        Map<String, Object> datos = new HashMap<>();

        datos.put("id", entity.getId());
        datos.put("numeroSolicitud", entity.getNumeroSolicitud());
        datos.put("socioId", entity.getSocioId());
        datos.put("tipoCreditoId", entity.getTipoCreditoId());
        datos.put("montoSolicitado", entity.getMontoSolicitado());
        datos.put("plazoMeses", entity.getPlazoMeses());
        datos.put("tasaInteresAplicada", entity.getTasaInteresAplicada());
        datos.put("cuotaMensualEstimada", entity.getCuotaMensualEstimada());
        datos.put("estado", entity.getEstado() != null ? entity.getEstado().name() : null);
        datos.put("fechaSolicitud", entity.getCreatedAt());
        datos.put("fechaAprobacion", entity.getFechaAprobacion());
        datos.put("fechaRechazo", entity.getFechaRechazo());
        datos.put("fechaDesembolso", entity.getFechaDesembolso());
        datos.put("motivoRechazo", entity.getMotivoRechazo());

        // Campos adicionales para PDF de pagaré
        datos.put("numeroCredito", entity.getNumeroSolicitud());
        datos.put("montoConcedido", entity.getMontoSolicitado());
        datos.put("tasaInteres", entity.getTasaInteresAplicada());

        return datos;
    }

    private Map<String, Object> mapAmortizacionEntityToMap(AmortizacionEntity entity) {
        Map<String, Object> datos = new HashMap<>();

        datos.put("id", entity.getId());
        datos.put("planId", entity.getPlanId());
        datos.put("numeroCuota", entity.getNumeroCuota());
        datos.put("fechaVencimiento", entity.getFechaVencimiento());
        datos.put("capital", entity.getCapital());
        datos.put("interes", entity.getInteres());
        datos.put("cuota", entity.getCuota());
        datos.put("saldoRestante", entity.getSaldoRestante());
        datos.put("estado", entity.getEstado() != null ? entity.getEstado().name() : null);
        datos.put("fechaPago", entity.getFechaPago());
        datos.put("referenciaPago", entity.getReferenciaPago());

        return datos;
    }

    /**
     * Excepciones específicas para el módulo de créditos.
     */
    public static class CreditoNoEncontradoException extends RuntimeException {
        public CreditoNoEncontradoException(String mensaje) {
            super(mensaje);
        }
    }

    public static class SolicitudNoEncontradaException extends RuntimeException {
        public SolicitudNoEncontradaException(String mensaje) {
            super(mensaje);
        }
    }

    public static class PlanNoEncontradoException extends RuntimeException {
        public PlanNoEncontradoException(String mensaje) {
            super(mensaje);
        }
    }
}
