// com/tufondo/creditos/application/usecase/SimularCreditoUseCase.java
package com.tufondo.creditos.application.usecase;

import com.tufondo.creditos.application.dto.SimulacionRequest;
import com.tufondo.creditos.application.dto.SimulacionResponse;
import com.tufondo.creditos.application.dto.SimulacionResponse.CuotaSimulada;
import com.tufondo.creditos.domain.model.PlanAmortizacion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Caso de uso para simular un crédito.
 * UC-CRE-08: Simular Crédito
 */
@Slf4j
@Service
public class SimularCreditoUseCase {

    public SimulacionResponse ejecutar(SimulacionRequest request, String ipOrigen) {
        return ejecutar(request.getMonto(), request.getPlazoMeses(), request.getTasa());
    }

    public SimulacionResponse ejecutar(BigDecimal monto, Integer plazoMeses, BigDecimal tasa) {
        // Calcular cuota usando sistema francés
        BigDecimal cuotaMensual = PlanAmortizacion.calcularCuotaFrances(monto, tasa, plazoMeses);
        
        // Calcular totales
        BigDecimal totalAPagar = cuotaMensual.multiply(new BigDecimal(plazoMeses));
        BigDecimal totalIntereses = totalAPagar.subtract(monto);

        // Generar tabla de amortización simulada
        List<CuotaSimulada> planSimulado = generarPlanSimulado(monto, tasa, plazoMeses, cuotaMensual);

        log.info("Simulación generada: {} USD, {} meses, {}% tasa", monto, plazoMeses, tasa);

        return SimulacionResponse.builder()
            .monto(monto)
            .plazoMeses(plazoMeses)
            .tasaInteresAnual(tasa)
            .cuotaMensual(cuotaMensual.setScale(4, RoundingMode.HALF_UP))
            .totalIntereses(totalIntereses.setScale(4, RoundingMode.HALF_UP))
            .totalAPagar(totalAPagar.setScale(4, RoundingMode.HALF_UP))
            .planSimulado(planSimulado)
            .nota("Simulación sin compromiso. Tasa final depende de evaluación crediticia.")
            .build();
    }

    private List<CuotaSimulada> generarPlanSimulado(BigDecimal principal, BigDecimal tasaAnual, 
            int meses, BigDecimal cuota) {
        List<CuotaSimulada> plan = new ArrayList<>();
        BigDecimal saldo = principal;
        BigDecimal tasaMensual = tasaAnual.divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);
        LocalDate fechaVencimiento = LocalDate.now().plusMonths(1);

        for (int i = 1; i <= meses; i++) {
            BigDecimal interes = saldo.multiply(tasaMensual).setScale(4, RoundingMode.HALF_UP);
            BigDecimal capital = cuota.subtract(interes);
            saldo = saldo.subtract(capital).setScale(4, RoundingMode.HALF_UP);
            if (saldo.compareTo(BigDecimal.ZERO) < 0) saldo = BigDecimal.ZERO;

            plan.add(CuotaSimulada.builder()
                .numeroCuota(i)
                .fechaVencimiento(fechaVencimiento.toString())
                .capital(capital)
                .interes(interes)
                .montoCuota(cuota)
                .saldoInsoluto(saldo)
                .build());

            fechaVencimiento = fechaVencimiento.plusMonths(1);
        }

        return plan;
    }
}
