// com/tufondo/beneficiarios/application/dto/BeneficiarioListResponseDTO.java
package com.tufondo.beneficiarios.application.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para la respuesta de lista de beneficiarios con resumen.
 */
public record BeneficiarioListResponseDTO(
        List<BeneficiarioResponseDTO> beneficiarios,
        int total,
        BigDecimal sumaPorcentajes
) {
    /**
     * Crea el DTO con la lista y calcula la suma de porcentajes.
     */
    public static BeneficiarioListResponseDTO of(List<BeneficiarioResponseDTO> beneficiarios) {
        BigDecimal suma = beneficiarios.stream()
                .map(BeneficiarioResponseDTO::porcentaje)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new BeneficiarioListResponseDTO(
                beneficiarios,
                beneficiarios.size(),
                suma
        );
    }
}