// com/tufondo/beneficiarios/application/usecase/GetBeneficiariosBySocioUseCase.java
package com.tufondo.beneficiarios.application.usecase;

import com.tufondo.beneficiarios.application.dto.BeneficiarioListResponseDTO;
import com.tufondo.beneficiarios.application.dto.BeneficiarioResponseDTO;
import com.tufondo.beneficiarios.application.port.SocioQueryPort;
import com.tufondo.beneficiarios.domain.exception.SocioNoEncontradoException;
import com.tufondo.beneficiarios.domain.model.Beneficiario;
import com.tufondo.beneficiarios.domain.repository.BeneficiarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Caso de uso para listar los beneficiarios activos de un socio.
 */
@Component
@RequiredArgsConstructor
public class GetBeneficiariosBySocioUseCase {

    private final BeneficiarioRepository repository;
    private final SocioQueryPort socioQueryPort;

    public BeneficiarioListResponseDTO ejecutar(UUID socioId) {
        // 1. Validar que el socio exista
        if (!socioQueryPort.existsByIdAndActivoTrue(socioId)) {
            throw new SocioNoEncontradoException(socioId);
        }

        // 2. Obtener beneficiarios activos
        List<Beneficiario> beneficiarios = repository.listarPorSocioId(socioId);

        // 3. Convertir a DTOs y retornar
        List<BeneficiarioResponseDTO> dtos = beneficiarios.stream()
                .map(BeneficiarioResponseDTO::fromDomain)
                .toList();

        return BeneficiarioListResponseDTO.of(dtos);
    }
}