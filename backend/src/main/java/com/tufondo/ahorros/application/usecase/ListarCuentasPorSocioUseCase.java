// com/tufondo/ahorros/application/usecase/ListarCuentasPorSocioUseCase.java
package com.tufondo.ahorros.application.usecase;

import com.tufondo.ahorros.application.dto.CuentasPorSocioResponse;
import com.tufondo.ahorros.application.mapper.AhorrosDTOMapper;
import com.tufondo.ahorros.domain.exception.AccesoCuentaAjenaException;
import com.tufondo.ahorros.domain.model.CuentaAhorro;
import com.tufondo.ahorros.domain.repository.CuentaAhorroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Caso de uso para listar cuentas por socio.
 */
@Service
@RequiredArgsConstructor
public class ListarCuentasPorSocioUseCase {

    private final CuentaAhorroRepository cuentaRepository;
    private final AhorrosDTOMapper mapper;

    public CuentasPorSocioResponse ejecutar(UUID socioId, UUID socioIdToken, boolean isAdmin) {
        // IDOR Check
        if (!isAdmin && !socioId.equals(socioIdToken)) {
            throw new AccesoCuentaAjenaException();
        }

        Pageable pageable = PageRequest.of(0, 100);
        Page<CuentaAhorro> cuentas = cuentaRepository.buscarPorSocioId(socioId, pageable);

        List<CuentasPorSocioResponse.CuentaResumen> resumenes = cuentas.getContent().stream()
                .map(c -> CuentasPorSocioResponse.CuentaResumen.builder()
                        .id(c.getId())
                        .numeroCuenta(c.getNumeroCuenta())
                        .saldoActual(c.getSaldoActual())
                        .estado(c.getEstado().name())
                        .tipoCuenta(c.getTipoCuenta().name())
                        .moneda(c.getMoneda().name())
                        .fechaApertura(c.getFechaApertura())
                        .build())
                .toList();

        return CuentasPorSocioResponse.builder()
                .socioId(socioId)
                .totalCuentas((int) cuentas.getTotalElements())
                .cuentas(resumenes)
                .build();
    }
}