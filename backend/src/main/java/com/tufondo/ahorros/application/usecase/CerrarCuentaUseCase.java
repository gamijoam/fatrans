// com/tufondo/ahorros/application/usecase/CerrarCuentaUseCase.java
package com.tufondo.ahorros.application.usecase;

import com.tufondo.ahorros.application.dto.CerrarCuentaResponse;
import com.tufondo.ahorros.domain.exception.CuentaAhorroNoEncontradaException;
import com.tufondo.ahorros.domain.exception.MovimientosPendientesException;
import com.tufondo.ahorros.domain.exception.SaldoNoCeroException;
import com.tufondo.ahorros.domain.model.CuentaAhorro;
import com.tufondo.ahorros.domain.model.enums.EstadoMovimiento;
import com.tufondo.ahorros.domain.repository.CuentaAhorroRepository;
import com.tufondo.ahorros.domain.repository.MovimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Caso de uso para cerrar una cuenta de ahorro.
 * V-006: Saldo actual debe ser 0.
 * V-007: No hay movimientos pendientes.
 */
@Service
@RequiredArgsConstructor
public class CerrarCuentaUseCase {

    private final CuentaAhorroRepository cuentaRepository;
    private final MovimientoRepository movimientoRepository;

    @Transactional
    public CerrarCuentaResponse ejecutar(String numeroCuenta, UUID socioIdToken, boolean isAdmin) {

        CuentaAhorro cuenta = cuentaRepository.buscarPorNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new CuentaAhorroNoEncontradaException(numeroCuenta));

        // IDOR Check
        if (!isAdmin && !cuenta.getSocioId().equals(socioIdToken)) {
            throw new com.tufondo.ahorros.domain.exception.AccesoCuentaAjenaException();
        }

        // Verificar saldo = 0 (V-006)
        if (!cuenta.puedeCerrarse()) {
            throw new SaldoNoCeroException(cuenta.getSaldoActual());
        }

        // Verificar no hay movimientos pendientes (V-007)
        long pendientes = movimientoRepository.contarPorCuentaYEstado(
                cuenta.getId(), EstadoMovimiento.PENDIENTE);
        if (pendientes > 0) {
            throw new MovimientosPendientesException(cuenta.getId());
        }

        // Cerrar cuenta
        cuenta.cerrar();
        cuenta = cuentaRepository.guardar(cuenta);

        return CerrarCuentaResponse.builder()
                .id(cuenta.getId())
                .numeroCuenta(cuenta.getNumeroCuenta())
                .estado(cuenta.getEstado().name())
                .fechaCierre(LocalDateTime.now())
                .saldoFinal(cuenta.getSaldoActual())
                .mensaje("Cuenta cerrada exitosamente")
                .build();
    }
}