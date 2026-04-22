// 📁 com/tufondo/socios/application/usecase/ObtenerSocioUseCase.java
// 🔧 MR-01 FIX: Usar SocioDTOMapper en vez de método local
package com.tufondo.socios.application.usecase;

import com.tufondo.socios.application.dto.SocioResponseDTO;
import com.tufondo.socios.domain.exception.SocioNoEncontradoException;
import com.tufondo.socios.domain.model.Socio;
import com.tufondo.socios.domain.repository.SocioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ObtenerSocioUseCase {

    private final SocioRepository socioRepository;
    private final SocioDTOMapper dtoMapper;

    @Transactional(readOnly = true)
    public SocioResponseDTO ejecutar(java.util.UUID id) {
        Socio socio = socioRepository.buscarPorId(id)
                .orElseThrow(() -> new SocioNoEncontradoException(id));
        return dtoMapper.toResponseDTO(socio);
    }

}
