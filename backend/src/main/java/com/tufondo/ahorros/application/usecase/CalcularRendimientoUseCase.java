// com/tufondo/ahorros/application/usecase/CalcularRendimientoUseCase.java
package com.tufondo.ahorros.application.usecase;

import com.tufondo.ahorros.application.dto.CalcularRendimientoRequest;
import com.tufondo.ahorros.application.dto.RendimientoResponse;
import com.tufondo.ahorros.application.mapper.AhorrosDTOMapper;
import com.tufondo.ahorros.domain.exception.*;
import com.tufondo.ahorros.domain.model.CuentaAhorro;
import com.tufondo.ahorros.domain.model.Rendimiento;
import com.tufondo.ahorros.domain.repository.CuentaAhorroRepository;
import com.tufondo.ahorros.domain.repository.RendimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso para calcular rendimiento de una cuenta.
 * RN-010: tasaAplicada debe estar en rango 0.0001 - 1.0 (CRÍTICO overflow).
 * RN-012: Un periodo no puede ser recalculado si ya fue aplicado.
 */
@Service
@RequiredArgsConstructor
public class CalcularRendimientoUseCase {

    private final CuentaAhorroRepository cuentaRepository;
    private final RendimientoRepository rendimientoRepository;
    private final AhorrosDTOMapper mapper;

    @Transactional
    public RendimientoResponse ejecutar(String numeroCuenta, CalcularRendimientoRequest request) {

        CuentaAhorro cuenta = cuentaRepository.buscarPorNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new CuentaAhorroNoEncontradaException(numeroCuenta));

        // Verificar RN-012: No recalcular si ya fue aplicado
        boolean existe = rendimientoRepository.existePorCuentaYPeriodoYTipo(
                cuenta.getId(), request.getPeriodoInicio(), request.getPeriodoFin(), request.getTipo());
        if (existe) {
            throw new RendimientoYaAplicadoException(
                    request.getPeriodoInicio().toString(), request.getPeriodoFin().toString());
        }

        // Validar tasa (RN-010)
        if (!Rendimiento.esTasaValida(cuenta.getTasaInteres())) {
            throw new TasaInvalidaException(cuenta.getTasaInteres());
        }

        // Calcular rendimiento
        Rendimiento rendimiento = Rendimiento.crear(
                cuenta.getId(),
                request.getPeriodoInicio(),
                request.getPeriodoFin(),
                cuenta.getSaldoActual(), // Usar saldo actual como aproximación
                cuenta.getTasaInteres(),
                request.getTipo()
        );

        rendimiento = rendimientoRepository.guardar(rendimiento);
        return mapper.toResponse(rendimiento);
    }
}