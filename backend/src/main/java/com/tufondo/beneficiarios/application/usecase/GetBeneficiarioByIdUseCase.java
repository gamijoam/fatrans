// com/tufondo/beneficiarios/application/usecase/GetBeneficiarioByIdUseCase.java
package com.tufondo.beneficiarios.application.usecase;

import com.tufondo.beneficiarios.application.dto.BeneficiarioResponseDTO;
import com.tufondo.beneficiarios.domain.exception.BeneficiarioNoEncontradoException;
import com.tufondo.beneficiarios.domain.model.Beneficiario;
import com.tufondo.beneficiarios.domain.repository.BeneficiarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso para obtener un beneficiario por su ID.
 */
@Component
@RequiredArgsConstructor
public class GetBeneficiarioByIdUseCase {

    private final BeneficiarioRepository repository;

    @Transactional(readOnly = true)
    public BeneficiarioResponseDTO ejecutar(UUID socioId, UUID beneficiarioId) {
        // 1. Buscar beneficiario
        Beneficiario beneficiario = repository.buscarPorId(beneficiarioId)
                .orElseThrow(() -> new BeneficiarioNoEncontradoException(beneficiarioId));

        // 2. Validar IDOR: el beneficiario debe pertenecer al socio
        if (!beneficiario.getSocioId().equals(socioId)) {
            throw new BeneficiarioNoEncontradoException(beneficiarioId);
        }

        // 3. Retornar DTO
        return BeneficiarioResponseDTO.fromDomain(beneficiario);
    }
}