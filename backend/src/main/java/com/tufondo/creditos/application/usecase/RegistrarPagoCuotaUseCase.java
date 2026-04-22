// com/tufondo/creditos/application/usecase/RegistrarPagoCuotaUseCase.java
package com.tufondo.creditos.application.usecase;

import com.tufondo.creditos.application.dto.PagoCuotaRequest;
import com.tufondo.creditos.application.dto.PagoCuotaResponse;
import com.tufondo.creditos.application.mapper.CreditosDTOMapper;
import com.tufondo.creditos.domain.exception.*;
import com.tufondo.creditos.domain.model.Amortizacion;
import com.tufondo.creditos.domain.model.PlanAmortizacion;
import com.tufondo.creditos.domain.model.SolicitudCredito;
import com.tufondo.creditos.domain.model.enums.EstadoAmortizacion;
import com.tufondo.creditos.domain.model.enums.EstadoSolicitud;
import com.tufondo.creditos.domain.repository.AmortizacionRepository;
import com.tufondo.creditos.domain.repository.CuentaGarantiaRepository;
import com.tufondo.creditos.domain.repository.PlanAmortizacionRepository;
import com.tufondo.creditos.domain.repository.SolicitudCreditoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Caso de uso para registrar el pago de una cuota.
 * UC-CRE-06: Registrar Pago de Cuota
 * Implementa prevención de double-payment con idempotencia y optimistic locking.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrarPagoCuotaUseCase {

    private final AmortizacionRepository amortizacionRepository;
    private final PlanAmortizacionRepository planRepository;
    private final SolicitudCreditoRepository solicitudRepository;
    private final CuentaGarantiaRepository cuentaGarantiaRepository;
    private final CreditosDTOMapper mapper;

    @Transactional
    public PagoCuotaResponse ejecutar(UUID cuotaId, PagoCuotaRequest request,
            UUID socioIdToken, boolean isAdmin, boolean isCajero, String ipOrigen) {
        
        // 1. Obtener amortización con lock (previene double-payment)
        Amortizacion amortizacion = amortizacionRepository.buscarPorIdWithLock(cuotaId)
            .orElseThrow(() -> new CuotaNoEncontradaException(cuotaId));
        
        // 2. Validar ownership - Prevenir IDOR
        // Un socio solo puede pagar cuotas de SU crédito, no de otros socios
        if (!isAdmin && !isCajero) {
            SolicitudCredito solicitud = solicitudRepository.buscarPorId(amortizacion.getPlan().getSolicitudId())
                .orElseThrow(() -> new CuotaSinSolicitudException(cuotaId.toString()));
            
            if (!solicitud.getSocioId().equals(socioIdToken)) {
                log.warn("Intento IDOR: Socio {} intentó pagar cuota {} de socio {}", 
                    socioIdToken, cuotaId, solicitud.getSocioId());
                throw new CreditoNoEncontradoException(cuotaId.toString());
            }
        }
        
        return ejecutar(cuotaId, request.getMonto(), request.getReferenciaPago(), 
            request.getCanalOrigen().name());
    }

    @Transactional
    public PagoCuotaResponse ejecutar(UUID cuotaId, BigDecimal monto, String referenciaPago, 
            String canalOrigen) {

        // 1. Verificar idempotencia
        if (referenciaPago != null && !referenciaPago.isEmpty()) {
            if (amortizacionRepository.existePorReferenciaPago(referenciaPago)) {
                throw new PagoDuplicadoException(referenciaPago);
            }
        }

        // 2. Obtener amortización con lock (previene double-payment)
        Amortizacion amortizacion = amortizacionRepository.buscarPorIdWithLock(cuotaId)
            .orElseThrow(() -> new CuotaNoEncontradaException(cuotaId));

        // 3. Verificar estado
        if (amortizacion.getEstado() != EstadoAmortizacion.PENDIENTE && 
            amortizacion.getEstado() != EstadoAmortizacion.VENCIDA) {
            throw new CuotaYaPagadaException();
        }

        // 4. Validar monto
        BigDecimal montoRequerido = amortizacion.getMontoTotalAPagar();
        if (monto.compareTo(montoRequerido) < 0) {
            throw new MontoInsuficienteException(monto, montoRequerido);
        }

        // 5. Registrar pago
        amortizacion.registrarPago(monto, LocalDate.now(), referenciaPago);
        amortizacionRepository.guardar(amortizacion);

        // 6. Actualizar plan
        PlanAmortizacion plan = planRepository.buscarPorId(amortizacion.getPlanId())
            .orElseThrow(() -> new PlanNoEncontradoException(amortizacion.getPlanId().toString()));
        plan.registrarPago(monto);
        planRepository.guardar(plan);

        // 7. Verificar si es última cuota para liberar colateral
        if (plan.estaCompletamentePagado()) {
            plan.marcarFinalizado();
            planRepository.guardar(plan);
            
            // Liberar colateral si existe
            SolicitudCredito solicitud = solicitudRepository.buscarPorId(plan.getSolicitudId())
                .orElseThrow(() -> new CreditoNoEncontradoException(plan.getSolicitudId().toString()));
            
            if (solicitud.tieneColateral() && solicitud.getColateralCuentaId() != null) {
                // Liberar el saldo retenido del colateral
                cuentaGarantiaRepository.liberarSaldo(
                    solicitud.getColateralCuentaId(), 
                    solicitud.getColateralMontoRetenido()
                );
                log.info("Colateral liberado: {} para solicitud {}", 
                    solicitud.getColateralMontoRetenido(), solicitud.getNumeroSolicitud());
            }
            
            // Transicionar solicitud a DESEMBOLSADO (crédito completado)
            solicitud.transicionarA(EstadoSolicitud.DESEMBOLSADO);
            solicitud.setUpdatedAt(LocalDateTime.now());
            solicitudRepository.guardar(solicitud);
        }

        log.info("Pago registrado: cuota {} por monto {} - Ref: {} - Canal: {}", 
            amortizacion.getNumeroCuota(), monto, referenciaPago, canalOrigen);

        return PagoCuotaResponse.builder()
            .id(amortizacion.getId().toString())
            .numeroCuota(amortizacion.getNumeroCuota())
            .estado(amortizacion.getEstado().name())
            .montoPagado(monto)
            .fechaPago(amortizacion.getFechaPago())
            .referenciaPago(referenciaPago)
            .saldoInsolutoRestante(plan.getSaldoPendiente())
            .mensaje("Pago registrado exitosamente")
            .build();
    }
}