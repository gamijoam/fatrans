// com/tufondo/ahorros/application/usecase/ConsultarSaldoUseCase.java
package com.tufondo.ahorros.application.usecase;

import com.tufondo.ahorros.application.dto.SaldoResponse;
import com.tufondo.ahorros.domain.exception.AccesoCuentaAjenaException;
import com.tufondo.ahorros.domain.exception.CuentaAhorroNoEncontradaException;
import com.tufondo.ahorros.domain.model.CuentaAhorro;
import com.tufondo.ahorros.domain.model.enums.TipoMovimiento;
import com.tufondo.ahorros.domain.repository.CuentaAhorroRepository;
import com.tufondo.ahorros.domain.repository.MovimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Caso de uso para consultar saldo de una cuenta.
 */
@Service
@RequiredArgsConstructor
public class ConsultarSaldoUseCase {

    private static final BigDecimal LIMITE_DEPOSITO = new BigDecimal("500000.00");
    private static final BigDecimal LIMITE_RETIRO_DIARIO = new BigDecimal("50000.00");

    private final CuentaAhorroRepository cuentaRepository;
    private final MovimientoRepository movimientoRepository;

    public SaldoResponse ejecutar(String numeroCuenta, UUID socioIdToken, boolean isAdmin) {
        CuentaAhorro cuenta = cuentaRepository.buscarPorNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new CuentaAhorroNoEncontradaException(numeroCuenta));

        // IDOR Check
        if (!isAdmin && !cuenta.getSocioId().equals(socioIdToken)) {
            throw new AccesoCuentaAjenaException();
        }

        // Calcular retiros del día
        LocalDateTime inicioDia = LocalDateTime.now().toLocalDate().atStartOfDay();
        BigDecimal retirosHoy = movimientoRepository.sumRetirosDelDiaPorSocio(
                cuenta.getSocioId(), inicioDia);
        if (retirosHoy == null) retirosHoy = BigDecimal.ZERO;

        BigDecimal retirosRestantes = LIMITE_RETIRO_DIARIO.subtract(retirosHoy);
        if (retirosRestantes.compareTo(BigDecimal.ZERO) < 0) {
            retirosRestantes = BigDecimal.ZERO;
        }

        return SaldoResponse.builder()
                .numeroCuenta(cuenta.getNumeroCuenta())
                .saldoActual(cuenta.getSaldoActual())
                .saldoRetenido(cuenta.getSaldoRetenido())
                .saldoDisponible(cuenta.getSaldoDisponible())
                .fechaConsulta(LocalDateTime.now())
                .limiteDeposito(LIMITE_DEPOSITO)
                .limiteRetiroDiario(LIMITE_RETIRO_DIARIO)
                .retirosRealizadosHoy(retirosHoy)
                .retirosRestantesHoy(retirosRestantes)
                .build();
    }
}