package com.tufondo.ahorros.infrastructure.contabilidad;

import com.tufondo.ahorros.application.port.output.AhorrosContabilidadPort;
import com.tufondo.ahorros.domain.model.CuentaAhorro;
import com.tufondo.ahorros.domain.model.Movimiento;
import com.tufondo.ahorros.domain.model.enums.Moneda;
import com.tufondo.contabilidad.application.dto.RegistrarAsientoCommand;
import com.tufondo.contabilidad.application.usecase.AsientoContableService;
import com.tufondo.contabilidad.domain.model.enums.OrigenAsiento;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Adapter que materializa cada operación de Ahorros como un asiento contable.
 *
 * <p>Implementación de {@link AhorrosContabilidadPort}. Vive en
 * {@code infrastructure/contabilidad} porque conoce los códigos VEN-NIF
 * concretos del plan de cuentas — esa es información de configuración
 * que no debe contaminar el dominio de Ahorros.</p>
 *
 * <h2>Códigos VEN-NIF usados (V21)</h2>
 * <table>
 *   <tr><th>Operación</th><th>Moneda</th><th>DEBE</th><th>HABER</th></tr>
 *   <tr><td>Depósito</td><td>VES</td><td>1.1.01 Caja Principal</td><td>2.1.01 Cuentas de Ahorro Bs</td></tr>
 *   <tr><td>Depósito</td><td>USD</td><td>1.1.05 Bancos Cuentas USD</td><td>2.1.02 Cuentas de Ahorro USD</td></tr>
 *   <tr><td>Retiro</td><td>VES</td><td>2.1.01 Cuentas de Ahorro Bs</td><td>1.1.01 Caja Principal</td></tr>
 *   <tr><td>Retiro</td><td>USD</td><td>2.1.02 Cuentas de Ahorro USD</td><td>1.1.05 Bancos Cuentas USD</td></tr>
 * </table>
 *
 * <p><strong>Razón del mapeo:</strong> un depósito incrementa Caja/Banco (Activo,
 * naturaleza deudora → DEBE) y simultáneamente incrementa la obligación con
 * el socio (Pasivo, naturaleza acreedora → HABER). El retiro es el espejo.</p>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AhorrosContabilidadAdapter implements AhorrosContabilidadPort {

    // ─── Códigos del plan de cuentas (V21) ─────────────────────────────────
    // Si el plan cambia, se actualiza acá. Son las únicas constantes que
    // acoplan Ahorros con el plan contable.
    static final String CUENTA_CAJA_BS         = "1.1.01";
    static final String CUENTA_BANCO_USD       = "1.1.05";
    static final String CUENTA_DEPOSITOS_BS    = "2.1.01";
    static final String CUENTA_DEPOSITOS_USD   = "2.1.02";

    private final AsientoContableService asientoContableService;

    @Override
    public void registrarDeposito(CuentaAhorro cuenta, Movimiento movimiento) {
        String cuentaActivo  = cuentaActivoPorMoneda(cuenta.getMoneda());
        String cuentaPasivo  = cuentaDepositosPorMoneda(cuenta.getMoneda());
        BigDecimal monto     = movimiento.getMonto();

        RegistrarAsientoCommand cmd = RegistrarAsientoCommand.builder()
                .fechaContable(LocalDate.now())
                .glosa(String.format("Depósito %s en cuenta %s",
                        movimiento.getNumeroOperacion(), cuenta.getNumeroCuenta()))
                .origen(OrigenAsiento.AHORRO_DEPOSITO)
                .referenciaExterna(movimiento.getNumeroOperacion())
                .creadoPorUsuarioId(null) // automático del sistema
                .asientoReversaId(null)
                .partidas(List.of(
                        // DEBE: aumenta el activo (caja o banco)
                        RegistrarAsientoCommand.Partida.builder()
                                .codigoCuenta(cuentaActivo)
                                .debe(monto)
                                .haber(null)
                                .glosa("Ingreso de efectivo del socio")
                                .build(),
                        // HABER: aumenta la obligación con el socio
                        RegistrarAsientoCommand.Partida.builder()
                                .codigoCuenta(cuentaPasivo)
                                .debe(null)
                                .haber(monto)
                                .glosa("Crédito en cuenta de ahorro")
                                .build()
                ))
                .build();

        asientoContableService.registrar(cmd);
        log.debug("Asiento de depósito generado: cuenta={} movimiento={} monto={}",
                cuenta.getNumeroCuenta(), movimiento.getNumeroOperacion(), monto);
    }

    @Override
    public void registrarRetiro(CuentaAhorro cuenta, Movimiento movimiento) {
        String cuentaActivo  = cuentaActivoPorMoneda(cuenta.getMoneda());
        String cuentaPasivo  = cuentaDepositosPorMoneda(cuenta.getMoneda());
        BigDecimal monto     = movimiento.getMonto();

        RegistrarAsientoCommand cmd = RegistrarAsientoCommand.builder()
                .fechaContable(LocalDate.now())
                .glosa(String.format("Retiro %s de cuenta %s",
                        movimiento.getNumeroOperacion(), cuenta.getNumeroCuenta()))
                .origen(OrigenAsiento.AHORRO_RETIRO)
                .referenciaExterna(movimiento.getNumeroOperacion())
                .creadoPorUsuarioId(null)
                .asientoReversaId(null)
                .partidas(List.of(
                        // DEBE: disminuye la obligación con el socio
                        RegistrarAsientoCommand.Partida.builder()
                                .codigoCuenta(cuentaPasivo)
                                .debe(monto)
                                .haber(null)
                                .glosa("Débito en cuenta de ahorro")
                                .build(),
                        // HABER: disminuye el activo (sale efectivo)
                        RegistrarAsientoCommand.Partida.builder()
                                .codigoCuenta(cuentaActivo)
                                .debe(null)
                                .haber(monto)
                                .glosa("Egreso de efectivo al socio")
                                .build()
                ))
                .build();

        asientoContableService.registrar(cmd);
        log.debug("Asiento de retiro generado: cuenta={} movimiento={} monto={}",
                cuenta.getNumeroCuenta(), movimiento.getNumeroOperacion(), monto);
    }

    // ─── Helpers de mapeo ──────────────────────────────────────────────────

    /** Cuenta de activo (caja/banco) según la moneda de la cuenta de ahorro. */
    private static String cuentaActivoPorMoneda(Moneda moneda) {
        if (moneda == null) {
            // default histórico: cuentas creadas antes del enum se asumen Bs.
            // Si esto se vuelve común, vale la pena un constraint NOT NULL en BD.
            return CUENTA_CAJA_BS;
        }
        return switch (moneda) {
            case VES -> CUENTA_CAJA_BS;
            case USD -> CUENTA_BANCO_USD;
        };
    }

    /** Cuenta de pasivo (captación de socios) según moneda. */
    private static String cuentaDepositosPorMoneda(Moneda moneda) {
        if (moneda == null) return CUENTA_DEPOSITOS_BS;
        return switch (moneda) {
            case VES -> CUENTA_DEPOSITOS_BS;
            case USD -> CUENTA_DEPOSITOS_USD;
        };
    }
}
