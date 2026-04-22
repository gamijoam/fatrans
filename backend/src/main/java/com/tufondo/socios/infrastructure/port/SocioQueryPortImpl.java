// com/tufondo/socios/infrastructure/port/SocioQueryPortImpl.java
package com.tufondo.socios.infrastructure.port;

import com.tufondo.beneficiarios.application.port.SocioQueryPort;
import com.tufondo.socios.domain.model.enums.EstadoSocio;
import com.tufondo.socios.domain.repository.SocioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Implementación del puerto SocioQueryPort para conectar con el módulo Beneficiarios.
 * Esta clase permite que el módulo Beneficiarios consulte información del socio titular
 * sin depender directamente del módulo Socios (desacoplamiento via puertos).
 */
@Component
@RequiredArgsConstructor
public class SocioQueryPortImpl implements SocioQueryPort {

    private final SocioRepository socioRepository;

    @Override
    public boolean existsByIdAndActivoTrue(UUID socioId) {
        return socioRepository.buscarPorId(socioId)
                .map(socio -> socio.getEstado() == EstadoSocio.ACTIVO)
                .orElse(false);
    }

    @Override
    public String getNumeroDocumentoById(UUID socioId) {
        return socioRepository.buscarPorId(socioId)
                .map(com.tufondo.socios.domain.model.Socio::getNumeroDocumento)
                .orElse(null);
    }
}