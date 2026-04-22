// ══════════════════════════════════════════════════════════════════════════════
// ARCHIVO: src/main/java/com/tufondo/socios/application/usecase/DesactivarSocioUseCase.java
// ══════════════════════════════════════════════════════════════════════════════
package com.tufondo.socios.application.usecase;

import com.tufondo.socios.application.dto.SocioResponseDTO;
import com.tufondo.socios.domain.exception.EstadoSocioInvalidoException;
import com.tufondo.socios.domain.exception.SocioNoEncontradoException;
import com.tufondo.socios.domain.model.Socio;
import com.tufondo.socios.domain.model.enums.EstadoSocio;
import com.tufondo.socios.domain.repository.SocioRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DesactivarSocioUseCase {

    private static final Logger log = LoggerFactory.getLogger(DesactivarSocioUseCase.class);

    private final SocioRepository socioRepository;
    private final SocioDTOMapper dtoMapper;

    /**
     * Desactiva un socio (lo pasa a estado INACTIVO).
     * Solo socios en estado ACTIVO pueden ser desactivados.
     * No elimina lógicamente al socio (soft-delete).
     *
     * @param id    ID del socio a desactivar
     * @param motivo motivo de la desactivación (opcional)
     * @return SocioResponseDTO del socio desactivado
     * @throws SocioNoEncontradoException si el socio no existe
     * @throws EstadoSocioInvalidoException si el socio no está en estado ACTIVO
     */
    @Transactional
    public SocioResponseDTO ejecutar(java.util.UUID id, String motivo) {
        log.info("Desactivando socio con ID: {}, motivo: {}", id, motivo);

        Socio socio = socioRepository.buscarPorId(id)
                .orElseThrow(() -> new SocioNoEncontradoException(id));

        // Validar transición: solo ACTIVO → INACTIVO
        if (socio.getEstado() != EstadoSocio.ACTIVO) {
            throw new EstadoSocioInvalidoException("Solo se pueden desactivar socios en estado ACTIVO");
        }

        // Ejecutar desactivación (método del dominio)
        socio.desactivar(motivo != null ? motivo.trim() : null);

        // Persistir
        Socio updated = socioRepository.guardar(socio);
        log.info("Socio con ID: {} desactivado exitosamente", id);

        return dtoMapper.toResponseDTO(updated);
    }
}
