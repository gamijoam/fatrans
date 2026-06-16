// com/tufondo/creditos/infrastructure/persistence/adapter/CuentaGarantiaRepositoryImpl.java
package com.tufondo.creditos.infrastructure.persistence.adapter;

import com.tufondo.ahorros.domain.model.CuentaAhorro;
import com.tufondo.ahorros.domain.repository.CuentaAhorroRepository;
import com.tufondo.creditos.domain.repository.CuentaGarantiaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Implementación del repositorio CuentaGarantia.
 * Esta implementación delega al módulo de Ahorros para obtener
 * información de las cuentas de garantía/colateral.
 *
 * Implementa el patrón Bridge para desacoplar el módulo de Créditos
 * del módulo de Ahorros.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class CuentaGarantiaRepositoryImpl implements CuentaGarantiaRepository {

    private final CuentaAhorroRepository cuentaAhorroRepository;

    @Override
    public BigDecimal obtenerSaldoDisponible(UUID cuentaId) {
        return cuentaAhorroRepository.buscarPorId(cuentaId)
            .map(CuentaAhorro::getSaldoDisponible)
            .orElse(BigDecimal.ZERO);
    }

    @Override
    public boolean verificarSaldoParaColateral(UUID cuentaId, BigDecimal montoRequerido) {
        return cuentaAhorroRepository.buscarPorId(cuentaId)
            .map(cuenta -> cuenta.tieneSaldoSuficiente(montoRequerido))
            .orElse(false);
    }

    @Override
    public void retenerSaldo(UUID cuentaId, BigDecimal monto) {
        cuentaAhorroRepository.buscarPorId(cuentaId)
            .ifPresent(cuenta -> {
                cuenta.retenerSaldo(monto);
                cuentaAhorroRepository.guardar(cuenta);
                log.info("Retenido saldo {} de cuenta {} para colateral de crédito", monto, cuentaId);
            });
    }

    @Override
    public void liberarSaldo(UUID cuentaId, BigDecimal monto) {
        cuentaAhorroRepository.buscarPorId(cuentaId)
            .ifPresent(cuenta -> {
                if (cuenta.getSaldoRetenido() != null && cuenta.getSaldoRetenido().compareTo(monto) >= 0) {
                    cuenta.liberarSaldoRetenido(monto);
                } else {
                    cuenta.agregarSaldo(monto);
                    log.warn("Liberando colateral legacy {} de cuenta {} sin saldo retenido suficiente", monto, cuentaId);
                }
                cuentaAhorroRepository.guardar(cuenta);
                log.info("Liberado saldo {} de cuenta {} (colateral de crédito)", monto, cuentaId);
            });
    }

    @Override
    public void transferirSaldo(UUID cuentaOrigenId, UUID cuentaDestinoId, BigDecimal monto) {
        CuentaAhorro origen = cuentaAhorroRepository.buscarPorId(cuentaOrigenId)
            .orElseThrow(() -> new IllegalStateException("Cuenta origen no encontrada: " + cuentaOrigenId));
        CuentaAhorro destino = cuentaAhorroRepository.buscarPorId(cuentaDestinoId)
            .orElseThrow(() -> new IllegalStateException("Cuenta destino no encontrada: " + cuentaDestinoId));

        origen.restarSaldo(monto);
        destino.agregarSaldo(monto);

        cuentaAhorroRepository.guardar(origen);
        cuentaAhorroRepository.guardar(destino);

        log.info("Transferido {} de cuenta {} a cuenta {} (ejecución colateral)",
            monto, cuentaOrigenId, cuentaDestinoId);
    }
}
