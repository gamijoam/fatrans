// ══════════════════════════════════════════════════════════════════════════════
// ARCHIVO: src/main/java/com/tufondo/socios/application/usecase/EliminarSocioUseCase.java
// ══════════════════════════════════════════════════════════════════════════════
package com.tufondo.socios.application.usecase;

import com.tufondo.socios.domain.exception.SocioNoEncontradoException;
import com.tufondo.socios.domain.model.Socio;
import com.tufondo.socios.domain.repository.SocioRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class EliminarSocioUseCase {

    private static final Logger log = LoggerFactory.getLogger(EliminarSocioUseCase.class);

    private final SocioRepository socioRepository;

    /**
     * Elimina lógicamente un socio (soft-delete).
     * El socio no se borra físicamente de la base de datos.
     * Registra el motivo de la eliminación si se proporciona.
     *
     * @param id     ID del socio a eliminar
     * @param motivo motivo de la eliminación (opcional, se guarda en motivoDesactivacion)
     * @throws SocioNoEncontradoException si el socio no existe
     */
    @Transactional
    public void ejecutar(java.util.UUID id, String motivo) {
        log.info("Eliminando (soft-delete) socio con ID: {}, motivo: {}", id, motivo);

        Socio socio = socioRepository.buscarPorId(id)
                .orElseThrow(() -> new SocioNoEncontradoException(id));

        // Marcar como eliminado lógicamente usando el método del dominio
        socio.eliminar(motivo);

        // Persistir
        socioRepository.guardar(socio);

        log.info("Socio con ID: {} marcado como eliminado lógicamente", id);
    }
}
