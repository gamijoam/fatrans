// com/tufondo/ahorros/application/usecase/RealizarRetiroUseCase.java
package com.tufondo.ahorros.application.usecase;

import com.tufondo.ahorros.application.dto.RetiroRequest;
import com.tufondo.ahorros.application.dto.MovimientoResponse;
import com.tufondo.ahorros.application.mapper.AhorrosDTOMapper;
import com.tufondo.ahorros.application.port.output.AhorrosContabilidadPort;
import com.tufondo.ahorros.domain.exception.*;
import com.tufondo.ahorros.domain.model.CuentaAhorro;
import com.tufondo.ahorros.domain.model.Movimiento;
import com.tufondo.ahorros.domain.model.enums.EstadoCuenta;
import com.tufondo.ahorros.domain.model.enums.TipoMovimiento;
import com.tufondo.ahorros.domain.model.valueobjects.NumeroOperacion;
import com.tufondo.ahorros.domain.repository.CuentaAhorroRepository;
import com.tufondo.ahorros.domain.repository.MovimientoRepository;
import com.tufondo.compliance.application.service.LocdoftOperacionService;
import com.tufondo.compliance.domain.model.ConsentimientoLocdoftOperacion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Caso de uso para realizar un retiro.
 * Límite: 50,000 MXN/día.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RealizarRetiroUseCase {

    private static final BigDecimal LIMITE_RETIRO = new BigDecimal("50000.00");

    private final CuentaAhorroRepository cuentaRepository;
    private final MovimientoRepository movimientoRepository;
    private final AhorrosDTOMapper mapper;
    private final LocdoftOperacionService locdoftService;
    private final AhorrosContabilidadPort contabilidadPort;

    @Transactional
    public MovimientoResponse ejecutar(String numeroCuenta, RetiroRequest request,
            UUID socioIdToken, boolean isAdmin, String ipOrigen, String sessionId, String requestId) {

        CuentaAhorro cuenta = cuentaRepository.buscarPorNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new CuentaAhorroNoEncontradaException(numeroCuenta));

        // IDOR Check
        if (!isAdmin && !cuenta.getSocioId().equals(socioIdToken)) {
            throw new AccesoCuentaAjenaException();
        }

        // Verificar que la cuenta permite operaciones (RN-005)
        if (cuenta.getEstado() != EstadoCuenta.ACTIVA) {
            throw new CuentaNoPermiteOperacionesException(numeroCuenta, cuenta.getEstado().name());
        }

        // Verificar saldo disponible
        if (!cuenta.tieneSaldoSuficiente(request.getMonto())) {
            throw new SaldoInsuficienteException(numeroCuenta);
        }

        // Verificar límite diario de retiro
        LocalDateTime inicioDia = LocalDateTime.now().toLocalDate().atStartOfDay();
        BigDecimal retirosHoy = movimientoRepository.sumRetirosDelDiaPorSocio(
                cuenta.getSocioId(), inicioDia);
        if (retirosHoy == null) retirosHoy = BigDecimal.ZERO;

        BigDecimal totalConEsteRetiro = retirosHoy.add(request.getMonto());
        if (totalConEsteRetiro.compareTo(LIMITE_RETIRO) > 0) {
            throw new MontoExcedeLimiteException(request.getMonto(), LIMITE_RETIRO, "retiro");
        }

        // LOCDOFT (#218 PR-C): mismo patrón que en depósito — si supera el
        // umbral, registra el consentimiento o lanza 422.
        ConsentimientoLocdoftOperacion consentimiento = locdoftService.validarYRegistrar(
                new LocdoftOperacionService.DatosOperacion(
                        cuenta.getSocioId(),
                        cuenta.getId(),
                        ConsentimientoLocdoftOperacion.TipoOperacion.RETIRO,
                        request.getMonto(),
                        cuenta.getMoneda() != null ? cuenta.getMoneda().name() : "VES",
                        Boolean.TRUE.equals(request.getConfirmaOrigenLicito()),
                        request.getOrigenFondos(),
                        ipOrigen, null, sessionId, requestId));

        // Ejecutar retiro
        BigDecimal saldoAnterior = cuenta.getSaldoActual();
        cuenta.restarSaldo(request.getMonto());
        cuentaRepository.guardar(cuenta);

        // Crear movimiento (RN-006: INMUTABLE) con retry para evitar colisiones
        String numeroOperacion;
        int intentos = 0;
        do {
            numeroOperacion = NumeroOperacion.generarValor();
            intentos++;
            if (intentos > 5) {
                throw new RuntimeException("No se pudo generar número de operación único después de 5 intentos");
            }
        } while (movimientoRepository.existePorNumeroOperacion(numeroOperacion));
        
        Movimiento movimiento = Movimiento.crearRetiro(
                cuenta.getId(),
                cuenta.getSocioId(),
                numeroOperacion,
                request.getMonto(),
                saldoAnterior,
                cuenta.getSaldoActual(),
                request.getCanalOrigen(),
                ipOrigen,
                sessionId,
                requestId
        );
        movimiento = movimientoRepository.guardar(movimiento);

        // Asociar consentimiento LOCDOFT con el movimiento real (resiliente).
        if (consentimiento != null) {
            locdoftService.asociarConMovimiento(consentimiento.getId(), movimiento.getId());
        }

        // Hook contable (#267): asiento de partida doble del retiro. Mismo
        // @Transactional → rollback completo si falla. Ver doc en
        // AhorrosContabilidadPort.
        contabilidadPort.registrarRetiro(cuenta, movimiento);

        log.info("Retiro realizado: {} de cuenta {}", request.getMonto(), numeroCuenta);
        return mapper.toResponse(movimiento);
    }
}