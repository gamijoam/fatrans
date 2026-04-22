// com/tufondo/beneficiarios/application.mapper/BeneficiarioDTOMapper.java
package com.tufondo.beneficiarios.application.mapper;

import com.tufondo.beneficiarios.application.dto.BeneficiarioResponseDTO;
import com.tufondo.beneficiarios.domain.model.Beneficiario;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre Beneficiario y BeneficiarioResponseDTO.
 */
@Component
public class BeneficiarioDTOMapper {

    /**
     * Convierte una entidad Beneficiario a BeneficiarioResponseDTO.
     */
    public BeneficiarioResponseDTO toResponseDTO(Beneficiario beneficiario) {
        return BeneficiarioResponseDTO.fromDomain(beneficiario);
    }
}