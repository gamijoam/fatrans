// com/tufondo/beneficiarios/application/usecase/ValidatePorcentajesSumUseCase.java
package com.tufondo.beneficiarios.application.usecase;

import com.tufondo.beneficiarios.domain.repository.BeneficiarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Caso de uso para validar que la suma de porcentajes de beneficiarios activos sea 100%.
 */
@Component
@RequiredArgsConstructor
public class ValidatePorcentajesSumUseCase {

    private static final BigDecimal CIEN = new BigDecimal("100.00");

    private final BeneficiarioRepository repository;

    /**
     * Valida que la suma de porcentajes sea exactamente 100%.
     *
     * @param socioId ID del socio
     * @param porcentajeExcluir Porcentaje a excluir del cálculo (para updates)
     * @return true si la suma es exactamente 100.00
     */
    public boolean ejecutar(UUID socioId, BigDecimal porcentajeExcluir) {
        BigDecimal sumaActual = repository.sumarPorcentajesPorSocioId(socioId);

        if (porcentajeExcluir != null) {
            sumaActual = sumaActual.subtract(porcentajeExcluir);
        }

        return sumaActual.compareTo(CIEN) == 0;
    }
}