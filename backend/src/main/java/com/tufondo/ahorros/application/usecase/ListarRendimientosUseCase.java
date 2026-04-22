// com/tufondo/ahorros/application/usecase/ListarRendimientosUseCase.java
package com.tufondo.ahorros.application.usecase;

import com.tufondo.ahorros.application.dto.RendimientoResponse;
import com.tufondo.ahorros.application.dto.RendimientosListResponse;
import com.tufondo.ahorros.application.mapper.AhorrosDTOMapper;
import com.tufondo.ahorros.domain.exception.AccesoCuentaAjenaException;
import com.tufondo.ahorros.domain.exception.CuentaAhorroNoEncontradaException;
import com.tufondo.ahorros.domain.model.CuentaAhorro;
import com.tufondo.ahorros.domain.model.Rendimiento;
import com.tufondo.ahorros.domain.repository.CuentaAhorroRepository;
import com.tufondo.ahorros.domain.repository.RendimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Caso de uso para listar rendimientos de una cuenta.
 */
@Service
@RequiredArgsConstructor
public class ListarRendimientosUseCase {

    private final CuentaAhorroRepository cuentaRepository;
    private final RendimientoRepository rendimientoRepository;
    private final AhorrosDTOMapper mapper;

    public RendimientosListResponse ejecutar(String numeroCuenta, UUID socioIdToken,
            boolean isAdmin, int page, int size) {

        CuentaAhorro cuenta = cuentaRepository.buscarPorNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new CuentaAhorroNoEncontradaException(numeroCuenta));

        // IDOR Check
        if (!isAdmin && !cuenta.getSocioId().equals(socioIdToken)) {
            throw new AccesoCuentaAjenaException();
        }

        int safeSize = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, safeSize);
        Page<Rendimiento> rendimientos = rendimientoRepository.buscarPorCuentaAhorroId(cuenta.getId(), pageable);

        List<RendimientoResponse> rendimientosDto = rendimientos.getContent().stream()
                .map(mapper::toResponse)
                .toList();

        return RendimientosListResponse.builder()
                .numeroCuenta(numeroCuenta)
                .pagina(page)
                .tamanio(safeSize)
                .totalElementos(rendimientos.getTotalElements())
                .totalPaginas(rendimientos.getTotalPages())
                .rendimientos(rendimientosDto)
                .build();
    }
}