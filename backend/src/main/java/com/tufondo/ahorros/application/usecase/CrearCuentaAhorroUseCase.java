// com/tufondo/ahorros/application/usecase/CrearCuentaAhorroUseCase.java
package com.tufondo.ahorros.application.usecase;

import com.tufondo.ahorros.application.dto.CreateCuentaAhorroRequest;
import com.tufondo.ahorros.application.dto.CuentaAhorroResponse;
import com.tufondo.ahorros.application.mapper.AhorrosDTOMapper;
import com.tufondo.ahorros.domain.exception.CuentaDuplicadaException;
import com.tufondo.ahorros.domain.exception.NumeroCuentaNoGeneradoException;
import com.tufondo.ahorros.domain.model.CuentaAhorro;
import com.tufondo.ahorros.domain.model.valueobjects.NumeroCuenta;
import com.tufondo.ahorros.domain.repository.CuentaAhorroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso para crear una cuenta de ahorro.
 * RN-001: Un socio solo puede tener una cuenta por tipo.
 */
@Service
@RequiredArgsConstructor
public class CrearCuentaAhorroUseCase {

    private final CuentaAhorroRepository cuentaRepository;
    private final AhorrosDTOMapper mapper;

    @Transactional
    public CuentaAhorroResponse ejecutar(CreateCuentaAhorroRequest request) {
        // Verificar RN-001: Un socio solo puede tener una cuenta por tipo
        if (cuentaRepository.existePorSocioIdYTipo(request.getSocioId(), request.getTipoCuenta())) {
            throw new CuentaDuplicadaException(request.getSocioId(), request.getTipoCuenta().name());
        }

        // Generar número de cuenta con formato AHO-YYYY-XXXXXX
        // Retry hasta 5 veces para evitar colisiones
        String numeroCuenta;
        int intentos = 0;
        do {
            numeroCuenta = NumeroCuenta.generarValor();
            intentos++;
            if (intentos > 5) {
                throw new NumeroCuentaNoGeneradoException();
            }
        } while (cuentaRepository.existePorNumeroCuenta(numeroCuenta));

        // Crear dominio y guardar
        CuentaAhorro cuenta = mapper.toDomain(request, numeroCuenta);
        cuenta = cuentaRepository.guardar(cuenta);

        return mapper.toResponse(cuenta);
    }
}