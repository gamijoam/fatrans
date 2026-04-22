// 📁 com/tufondo/socios/application/usecase/ActivarSocioUseCase.java
// 🔧 MR-04 FIX: UseCase dedicado para activar socio (endpoint POST /{id}/activar)
package com.tufondo.socios.application.usecase;

import com.tufondo.socios.application.dto.SocioResponseDTO;
import com.tufondo.socios.domain.exception.EstadoSocioInvalidoException;
import com.tufondo.socios.domain.exception.SocioNoEncontradoException;
import com.tufondo.socios.domain.model.Socio;
import com.tufondo.socios.domain.model.enums.EstadoSocio;
import com.tufondo.socios.domain.repository.SocioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ActivarSocioUseCase {

    private final SocioRepository socioRepository;
    private final SocioDTOMapper dtoMapper;

    /**
     * Reactiva un socio (cambia estado a ACTIVO y desmarca soft-delete).
     * Solo aplicable a socios con estado INACTIVO o PENDIENTE_APROBACION.
     */
    @Transactional
    public SocioResponseDTO ejecutar(java.util.UUID id) {
        Socio socio = socioRepository.buscarPorId(id)
                .orElseThrow(() -> new SocioNoEncontradoException(id));

        // Solo se puede activar desde INACTIVO o PENDIENTE_APROBACION
        if (socio.getEstado() == EstadoSocio.ACTIVO) {
            throw new EstadoSocioInvalidoException(
                    "El socio con ID " + id + " ya está activo");
        }

        socio.activar();
        Socio guardado = socioRepository.guardar(socio);
        return dtoMapper.toResponseDTO(guardado);
    }

    /**
     * Aprueba un socio pendiente (cambia de PENDIENTE_APROBACION a ACTIVO).
     */
    @Transactional
    public SocioResponseDTO aprobar(java.util.UUID id) {
        Socio socio = socioRepository.buscarPorId(id)
                .orElseThrow(() -> new SocioNoEncontradoException(id));

        if (socio.getEstado() != EstadoSocio.PENDIENTE_APROBACION) {
            throw new EstadoSocioInvalidoException(
                    "Solo se pueden aprobar socios en estado PENDIENTE_APROBACION. "
                    + "Estado actual: " + socio.getEstado());
        }

        socio.activar(); // Esto también cambia estado a ACTIVO
        Socio guardado = socioRepository.guardar(socio);
        return dtoMapper.toResponseDTO(guardado);
    }
}
