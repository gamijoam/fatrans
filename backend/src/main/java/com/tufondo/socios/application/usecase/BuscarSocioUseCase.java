// 📁 com/tufondo/socios/application/usecase/BuscarSocioUseCase.java
// 🔧 MR-08 FIX: UseCase de búsqueda por filtros dinámicos
// Implementación nueva
package com.tufondo.socios.application.usecase;

import com.tufondo.socios.application.dto.SocioResponseDTO;
import com.tufondo.socios.domain.model.Socio;
import com.tufondo.socios.domain.repository.SocioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BuscarSocioUseCase {

    private final SocioRepository socioRepository;
    private final SocioDTOMapper dtoMapper;

    @Transactional(readOnly = true)
    public Page<Socio> ejecutar(String nombre, String apellido, String numeroDocumento, 
                                 String numeroSocio, String correo, org.springframework.data.domain.Pageable pageable) {
        return socioRepository.buscarPorCriterios(nombre, apellido, numeroDocumento, numeroSocio, correo, pageable);
    }

    @Transactional(readOnly = true)
    public List<SocioResponseDTO> buscarPorNumeroDocumento(String numeroDocumento) {
        return socioRepository
                .buscarPorCriterios(null, null, numeroDocumento, null, null, org.springframework.data.domain.PageRequest.of(0, 1))
                .stream()
                .map(dtoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SocioResponseDTO> buscarPorCorreo(String correo) {
        return socioRepository
                .buscarPorCorreo(correo)
                .map(dtoMapper::toResponseDTO)
                .map(List::of)
                .orElse(List.of());
    }
}
