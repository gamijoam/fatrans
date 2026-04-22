// com/tufondo/ahorros/application/usecase/CalcularRendimientosBatchUseCase.java
package com.tufondo.ahorros.application.usecase;

import com.tufondo.ahorros.application.dto.CalcularBatchRequest;
import com.tufondo.ahorros.application.dto.CalcularBatchResponse;
import com.tufondo.ahorros.application.dto.RendimientoResponse;
import com.tufondo.ahorros.application.mapper.AhorrosDTOMapper;
import com.tufondo.ahorros.domain.exception.CuentaNoPermiteOperacionesException;
import com.tufondo.ahorros.domain.exception.RendimientoYaAplicadoException;
import com.tufondo.ahorros.domain.exception.TasaInvalidaException;
import com.tufondo.ahorros.domain.model.CuentaAhorro;
import com.tufondo.ahorros.domain.model.Rendimiento;
import com.tufondo.ahorros.domain.model.enums.EstadoCuenta;
import com.tufondo.ahorros.domain.repository.CuentaAhorroRepository;
import com.tufondo.ahorros.domain.repository.RendimientoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Caso de uso para calcular rendimientos en batch.
 * Implementa Saga Pattern para procesamiento idempotente.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CalcularRendimientosBatchUseCase {

    private final CuentaAhorroRepository cuentaRepository;
    private final RendimientoRepository rendimientoRepository;
    private final AhorrosDTOMapper mapper;

    @Transactional
    public CalcularBatchResponse ejecutar(CalcularBatchRequest request, UUID socioIdToken, boolean isAdmin) {
        int totalCuentas = request.getCuentaIds().size();
        int exitosas = 0;
        int fallidas = 0;
        List<CalcularBatchResponse.ResultadoCuenta> resultados = new ArrayList<>();

        for (UUID cuentaId : request.getCuentaIds()) {
            try {
                Optional<CuentaAhorro> optCuenta = cuentaRepository.buscarPorId(cuentaId);
                if (optCuenta.isEmpty()) {
                    resultados.add(CalcularBatchResponse.ResultadoCuenta.builder()
                            .cuentaId(cuentaId)
                            .numeroCuenta("N/A")
                            .exitoso(false)
                            .error("CUENTA_NO_ENCONTRADA")
                            .build());
                    fallidas++;
                    continue;
                }

                CuentaAhorro cuenta = optCuenta.get();

                // IDOR Check - Solo admins pueden procesar cuentas ajenas
                if (!isAdmin && !cuenta.getSocioId().equals(socioIdToken)) {
                    throw new com.tufondo.ahorros.domain.exception.AccesoCuentaAjenaException();
                }

                // Verificar que la cuenta permite rendimientos
                if (cuenta.getEstado() != EstadoCuenta.ACTIVA) {
                    throw new CuentaNoPermiteOperacionesException(cuenta.getNumeroCuenta(), 
                            cuenta.getEstado().name());
                }

                // Verificar si ya existe rendimiento para el periodo
                boolean existe = rendimientoRepository.existePorCuentaYPeriodoYTipo(
                        cuentaId, request.getPeriodoInicio(), request.getPeriodoFin(), request.getTipo());
                if (existe) {
                    throw new RendimientoYaAplicadoException(
                            request.getPeriodoInicio().toString(), request.getPeriodoFin().toString());
                }

                // Validar tasa
                if (!Rendimiento.esTasaValida(cuenta.getTasaInteres())) {
                    throw new TasaInvalidaException(cuenta.getTasaInteres());
                }

                // Calcular rendimiento
                Rendimiento rendimiento = Rendimiento.crear(
                        cuentaId,
                        request.getPeriodoInicio(),
                        request.getPeriodoFin(),
                        cuenta.getSaldoActual(),
                        cuenta.getTasaInteres(),
                        request.getTipo()
                );
                rendimiento = rendimientoRepository.guardar(rendimiento);

                resultados.add(CalcularBatchResponse.ResultadoCuenta.builder()
                        .cuentaId(cuentaId)
                        .numeroCuenta(cuenta.getNumeroCuenta())
                        .exitoso(true)
                        .rendimientoId(rendimiento.getId())
                        .build());
                exitosas++;

            } catch (CuentaNoPermiteOperacionesException | RendimientoYaAplicadoException | TasaInvalidaException e) {
                log.warn("Error de negocio calculando rendimiento para cuenta {}: {}", cuentaId, e.getMessage());
                resultados.add(CalcularBatchResponse.ResultadoCuenta.builder()
                        .cuentaId(cuentaId)
                        .numeroCuenta("N/A")
                        .exitoso(false)
                        .error(e.getMessage())
                        .build());
                fallidas++;
            } catch (Exception e) {
                log.error("Error inesperado calculando rendimiento para cuenta {}", cuentaId, e);
                throw e;
            }
        }

        return CalcularBatchResponse.builder()
                .totalCuentas(totalCuentas)
                .procesadas(exitosas + fallidas)
                .exitosas(exitosas)
                .fallidas(fallidas)
                .resultados(resultados)
                .fechaProcesamiento(LocalDateTime.now())
                .build();
    }
}