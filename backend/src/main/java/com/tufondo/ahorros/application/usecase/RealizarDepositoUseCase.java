// com/tufondo/ahorros/application/usecase/RealizarDepositoUseCase.java
package com.tufondo.ahorros.application.usecase;

import com.tufondo.ahorros.application.dto.DepositoRequest;
import com.tufondo.ahorros.application.dto.MovimientoResponse;
import com.tufondo.ahorros.application.mapper.AhorrosDTOMapper;
import com.tufondo.ahorros.application.port.output.AhorrosContabilidadPort;
import com.tufondo.ahorros.domain.exception.AccesoCuentaAjenaException;
import com.tufondo.ahorros.domain.exception.CuentaAhorroNoEncontradaException;
import com.tufondo.ahorros.domain.exception.CuentaNoPermiteOperacionesException;
import com.tufondo.ahorros.domain.exception.MontoExcedeLimiteException;
import com.tufondo.ahorros.domain.model.CuentaAhorro;
import com.tufondo.ahorros.domain.model.Movimiento;
import com.tufondo.ahorros.domain.model.enums.EstadoCuenta;
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
import java.util.UUID;

/**
 * Caso de uso para realizar un depósito.
 * Límite: 500,000 MXN por operación.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RealizarDepositoUseCase {

    private static final BigDecimal LIMITE_DEPOSITO = new BigDecimal("500000.00");

    private final CuentaAhorroRepository cuentaRepository;
    private final MovimientoRepository movimientoRepository;
    private final AhorrosDTOMapper mapper;
    private final LocdoftOperacionService locdoftService;
    private final AhorrosContabilidadPort contabilidadPort;

    @Transactional
    public MovimientoResponse ejecutar(String numeroCuenta, DepositoRequest request,
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

        // Verificar límite de depósito
        if (request.getMonto().compareTo(LIMITE_DEPOSITO) > 0) {
            throw new MontoExcedeLimiteException(request.getMonto(), LIMITE_DEPOSITO, "depósito");
        }

        // LOCDOFT (#218 PR-C): si supera el umbral, registra el consentimiento
        // o lanza LocdoftConsentimientoRequeridoException (HTTP 422 → frontend
        // abre modal con la pregunta de origen lícito).
        ConsentimientoLocdoftOperacion consentimiento = locdoftService.validarYRegistrar(
                new LocdoftOperacionService.DatosOperacion(
                        cuenta.getSocioId(),
                        cuenta.getId(),
                        ConsentimientoLocdoftOperacion.TipoOperacion.DEPOSITO,
                        request.getMonto(),
                        cuenta.getMoneda() != null ? cuenta.getMoneda().name() : "VES",
                        Boolean.TRUE.equals(request.getConfirmaOrigenLicito()),
                        request.getOrigenFondos(),
                        ipOrigen, null, sessionId, requestId));

        // Ejecutar depósito
        BigDecimal saldoAnterior = cuenta.getSaldoActual();
        cuenta.agregarSaldo(request.getMonto());
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
        
        Movimiento movimiento = Movimiento.crearDeposito(
                cuenta.getId(),
                cuenta.getSocioId(),
                numeroOperacion,
                request.getMonto(),
                saldoAnterior,
                cuenta.getSaldoActual(),
                request.getCanalOrigen(),
                ipOrigen,
                sessionId,
                requestId,
                request.getDescripcion(),
                request.getReferencia()
        );
        movimiento = movimientoRepository.guardar(movimiento);

        // Asociar consentimiento LOCDOFT con el movimiento real recién creado
        // (resiliente: si falla, queda con movimiento_id=null, no aborta).
        if (consentimiento != null) {
            locdoftService.asociarConMovimiento(consentimiento.getId(), movimiento.getId());
        }

        // Hook contable (#267): genera el asiento partida doble. Corre en el
        // mismo @Transactional — si falla, rollback completo (saldo + movimiento
        // + asiento revierten juntos). NO es best-effort: la contabilidad debe
        // estar sincronizada con la caja por exigencia regulatoria SUDECA.
        contabilidadPort.registrarDeposito(cuenta, movimiento);

        log.info("Depósito realizado: {} en cuenta {}", request.getMonto(), numeroCuenta);
        return mapper.toResponse(movimiento);
    }
}