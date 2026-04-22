// com/tufondo/ahorros/application/usecase/ObtenerCuentaUseCase.java
package com.tufondo.ahorros.application.usecase;

import com.tufondo.ahorros.application.dto.CuentaAhorroResponse;
import com.tufondo.ahorros.application.mapper.AhorrosDTOMapper;
import com.tufondo.ahorros.domain.exception.AccesoCuentaAjenaException;
import com.tufondo.ahorros.domain.exception.CuentaAhorroNoEncontradaException;
import com.tufondo.ahorros.domain.model.CuentaAhorro;
import com.tufondo.ahorros.domain.repository.CuentaAhorroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Caso de uso para consultar una cuenta de ahorro.
 */
@Service
@RequiredArgsConstructor
public class ObtenerCuentaUseCase {

    private final CuentaAhorroRepository cuentaRepository;
    private final AhorrosDTOMapper mapper;

    public CuentaAhorroResponse ejecutar(String numeroCuenta, UUID socioIdToken, boolean isAdmin) {
        CuentaAhorro cuenta = cuentaRepository.buscarPorNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new CuentaAhorroNoEncontradaException(numeroCuenta));

        // IDOR Check: Verificar que el socio tiene acceso a la cuenta
        if (!isAdmin && !cuenta.getSocioId().equals(socioIdToken)) {
            throw new AccesoCuentaAjenaException();
        }

        return mapper.toResponse(cuenta);
    }
}