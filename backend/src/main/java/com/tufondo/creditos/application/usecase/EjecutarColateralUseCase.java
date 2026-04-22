// com/tufondo/creditos/application/usecase/EjecutarColateralUseCase.java
package com.tufondo.creditos.application.usecase;

import com.tufondo.creditos.domain.exception.CreditoNoEncontradoException;
import com.tufondo.creditos.domain.exception.CuotaNoEncontradaException;
import com.tufondo.creditos.domain.exception.EstadoCreditoInvalidoException;
import com.tufondo.creditos.domain.model.Amortizacion;
import com.tufondo.creditos.domain.model.PlanAmortizacion;
import com.tufondo.creditos.domain.model.SolicitudCredito;
import com.tufondo.creditos.domain.model.enums.EstadoAmortizacion;
import com.tufondo.creditos.domain.model.enums.EstadoPlanAmortizacion;
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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Caso de uso para ejecutar el colateral de una cuota en mora.
 * UC-CRE-07: Ejecutar Colateral por Mora > 90 días
 * 
 * Este caso de uso se ejecuta cuando una cuota ha estado en mora por más de 90 días
 * y el colateral debe ser ejecutado para cubrir el adeudo.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EjecutarColateralUseCase {

    private final AmortizacionRepository amortizacionRepository;
    private final PlanAmortizacionRepository planRepository;
    private final SolicitudCreditoRepository solicitudRepository;
    private final CuentaGarantiaRepository cuentaGarantiaRepository;

    /**
     * Ejecuta el colateral para una cuota específica.
     * 
     * @param cuotaId ID de la cuota a ejecutar
     * @return Map con detalles de la ejecución
     * @throws CuotaNoEncontradaException si la cuota no existe
     * @throws EstadoCreditoInvalidoException si la cuota no está en estado válido para ejecución
     */
    @Transactional
    public Map<String, Object> ejecutar(UUID cuotaId) {
        // 1. Obtener amortización
        Amortizacion amortizacion = amortizacionRepository.buscarPorId(cuotaId)
            .orElseThrow(() -> new CuotaNoEncontradaException(cuotaId));

        // 2. Validar que puede ejecutar colateral
        if (!amortizacion.puedeEjecutarColateral()) {
            throw new IllegalStateException(String.format(
                "La cuota %s no puede ejecutar colateral. Estado: %s, Días mora: %d. Requiere: > 90 días en estado CURSO_MORA",
                cuotaId, amortizacion.getEstado(), amortizacion.getDiasMora()));
        }

        // 3. Obtener el plan y la solicitud
        PlanAmortizacion plan = planRepository.buscarPorId(amortizacion.getPlanId())
            .orElseThrow(() -> new RuntimeException("Plan no encontrado"));
        
        SolicitudCredito solicitud = solicitudRepository.buscarPorId(plan.getSolicitudId())
            .orElseThrow(() -> new CreditoNoEncontradoException(plan.getSolicitudId().toString()));

        // 4. Verificar que la solicitud tiene colateral configurado
        if (!solicitud.tieneColateral() || solicitud.getColateralCuentaId() == null) {
            throw new RuntimeException("La solicitud no tiene colateral configurado para ejecutar");
        }

        // 5. Calcular monto a ejecutar (capital + intereses pendientes)
        BigDecimal montoEjecutar = calcularMontoAEjecutar(amortizacion, plan);

        // 6. Ejecutar transferencia desde colateral a cuenta de créditos
        cuentaGarantiaRepository.transferirSaldo(
            solicitud.getColateralCuentaId(),
            solicitud.getColateralCuentaId(), // La implementación decidirá la cuenta destino
            montoEjecutar
        );

        // 7. Marcar la amortización como ejecutada
        amortizacion.marcarEjecutada(montoEjecutar, amortizacion.getDiasMora());
        amortizacionRepository.guardar(amortizacion);

        // 8. Actualizar el plan
        plan.registrarPago(montoEjecutar);
        planRepository.guardar(plan);

        // 9. Verificar si todas las cuotas están pagadas/ejecutadas
        if (plan.estaCompletamentePagado()) {
            plan.marcarFinalizado();
            planRepository.guardar(plan);
            
            // Transicionar solicitud a COLATERAL_EJECUTADO
            solicitud.transicionarA(EstadoSolicitud.COLATERAL_EJECUTADO);
            solicitud.setUpdatedAt(LocalDateTime.now());
            solicitudRepository.guardar(solicitud);
            
            log.info("Crédito finalizado por ejecución de colateral: {}", solicitud.getNumeroSolicitud());
        }

        log.info("Colateral ejecutado: {} por monto {} para cuota {} - Días mora: {}", 
            solicitud.getNumeroSolicitud(), montoEjecutar, amortizacion.getNumeroCuota(), amortizacion.getDiasMora());

        // 10. Retornar respuesta
        Map<String, Object> response = new HashMap<>();
        response.put("cuotaId", cuotaId.toString());
        response.put("numeroCuota", amortizacion.getNumeroCuota());
        response.put("solicitudId", solicitud.getId().toString());
        response.put("numeroSolicitud", solicitud.getNumeroSolicitud());
        response.put("montoEjecutado", montoEjecutar);
        response.put("diasMora", amortizacion.getDiasMora());
        response.put("estadoAmortizacion", EstadoAmortizacion.EJECUTADA.name());
        response.put("colateralLiberado", false); // Se libera al finalizar el crédito
        response.put("mensaje", String.format("Colateral ejecutado: %s por %d días de mora", 
            montoEjecutar, amortizacion.getDiasMora()));
        response.put("fechaEjecucion", LocalDateTime.now().toString());
        
        return response;
    }

    /**
     * Calcula el monto total a ejecutar:
     * capital pendiente + intereses normales + intereses de mora.
     */
    private BigDecimal calcularMontoAEjecutar(Amortizacion amortizacion, PlanAmortizacion plan) {
        BigDecimal capital = amortizacion.getCapital() != null ? amortizacion.getCapital() : BigDecimal.ZERO;
        BigDecimal interes = amortizacion.getInteres() != null ? amortizacion.getInteres() : BigDecimal.ZERO;
        BigDecimal mora = amortizacion.getInteresMora() != null ? amortizacion.getInteresMora() : BigDecimal.ZERO;
        
        return capital.add(interes).add(mora);
    }
}