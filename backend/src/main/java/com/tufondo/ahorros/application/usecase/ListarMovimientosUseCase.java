// com/tufondo/ahorros/application/usecase/ListarMovimientosUseCase.java
package com.tufondo.ahorros.application.usecase;

import com.tufondo.ahorros.application.dto.MovimientoResponse;
import com.tufondo.ahorros.application.dto.MovimientosListResponse;
import com.tufondo.ahorros.application.mapper.AhorrosDTOMapper;
import com.tufondo.ahorros.domain.exception.AccesoCuentaAjenaException;
import com.tufondo.ahorros.domain.exception.CuentaAhorroNoEncontradaException;
import com.tufondo.ahorros.domain.model.CuentaAhorro;
import com.tufondo.ahorros.domain.model.Movimiento;
import com.tufondo.ahorros.domain.model.enums.TipoMovimiento;
import com.tufondo.ahorros.domain.repository.CuentaAhorroRepository;
import com.tufondo.ahorros.domain.repository.MovimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Caso de uso para listar movimientos de una cuenta.
 */
@Service
@RequiredArgsConstructor
public class ListarMovimientosUseCase {

    private final CuentaAhorroRepository cuentaRepository;
    private final MovimientoRepository movimientoRepository;
    private final AhorrosDTOMapper mapper;

    public MovimientosListResponse ejecutar(String numeroCuenta, UUID socioIdToken, boolean isAdmin,
            int page, int size, LocalDate fechaInicio, LocalDate fechaFin, TipoMovimiento tipo) {

        CuentaAhorro cuenta = cuentaRepository.buscarPorNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new CuentaAhorroNoEncontradaException(numeroCuenta));

        // IDOR Check
        if (!isAdmin && !cuenta.getSocioId().equals(socioIdToken)) {
            throw new AccesoCuentaAjenaException();
        }

        // Limitar tamaño de página
        int safeSize = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, safeSize);

        Page<Movimiento> movimientos;
        if (fechaInicio != null && fechaFin != null) {
            LocalDateTime ini = fechaInicio.atStartOfDay();
            LocalDateTime fin = fechaFin.plusDays(1).atStartOfDay();
            movimientos = movimientoRepository.buscarPorCuentaYRangoFechas(cuenta.getId(), ini, fin, pageable);
        } else if (tipo != null) {
            movimientos = movimientoRepository.buscarPorCuentaYTipo(cuenta.getId(), tipo, pageable);
        } else {
            movimientos = movimientoRepository.buscarPorCuentaAhorroId(cuenta.getId(), pageable);
        }

        List<MovimientoResponse> movimientosDto = movimientos.getContent().stream()
                .map(mapper::toResponse)
                .toList();

        return MovimientosListResponse.builder()
                .numeroCuenta(numeroCuenta)
                .pagina(page)
                .tamanio(safeSize)
                .totalElementos(movimientos.getTotalElements())
                .totalPaginas(movimientos.getTotalPages())
                .movimientos(movimientosDto)
                .build();
    }
}