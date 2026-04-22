// com/tufondo/ahorros/application/usecase/ObtenerMovimientoDetalleUseCase.java
package com.tufondo.ahorros.application.usecase;

import com.tufondo.ahorros.application.dto.MovimientoResponse;
import com.tufondo.ahorros.application.mapper.AhorrosDTOMapper;
import com.tufondo.ahorros.domain.exception.AccesoCuentaAjenaException;
import com.tufondo.ahorros.domain.exception.CuentaAhorroNoEncontradaException;
import com.tufondo.ahorros.domain.exception.MovimientoNoEncontradoException;
import com.tufondo.ahorros.domain.model.CuentaAhorro;
import com.tufondo.ahorros.domain.model.Movimiento;
import com.tufondo.ahorros.domain.repository.CuentaAhorroRepository;
import com.tufondo.ahorros.domain.repository.MovimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Caso de uso para obtener detalle de un movimiento.
 */
@Service
@RequiredArgsConstructor
public class ObtenerMovimientoDetalleUseCase {

    private final CuentaAhorroRepository cuentaRepository;
    private final MovimientoRepository movimientoRepository;
    private final AhorrosDTOMapper mapper;

    public MovimientoResponse ejecutar(String numeroCuenta, String numeroOperacion,
            UUID socioIdToken, boolean isAdmin) {

        CuentaAhorro cuenta = cuentaRepository.buscarPorNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new CuentaAhorroNoEncontradaException(numeroCuenta));

        // IDOR Check
        if (!isAdmin && !cuenta.getSocioId().equals(socioIdToken)) {
            throw new AccesoCuentaAjenaException();
        }

        Movimiento movimiento = movimientoRepository.buscarPorNumeroOperacion(numeroOperacion)
                .orElseThrow(() -> new MovimientoNoEncontradoException(numeroOperacion));

        // Verificar que el movimiento pertenece a la cuenta
        if (!movimiento.getCuentaAhorroId().equals(cuenta.getId())) {
            throw new MovimientoNoEncontradoException(numeroOperacion);
        }

        return mapper.toResponse(movimiento);
    }
}