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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso para crear una cuenta de ahorro.
 * RN-001: Un socio solo puede tener una cuenta por tipo.
 */
@Service
@RequiredArgsConstructor
public class CrearCuentaAhorroUseCase {

    private final CuentaAhorroRepository cuentaRepository;
    private final AhorrosDTOMapper mapper;

    /**
     * @deprecated Issue #179: usar {@link #ejecutar(CreateCuentaAhorroRequest, UUID, boolean)}
     * que valida ownership. Esta firma permitía IDOR (un socio creando cuenta a nombre
     * de otro). Mantenida temporalmente para compatibilidad de tests legacy; será removida.
     */
    @Deprecated(forRemoval = true)
    @Transactional
    public CuentaAhorroResponse ejecutar(CreateCuentaAhorroRequest request) {
        // Llamar al método con check de ownership desactivado (modo "sistema/admin").
        // SOLO para llamadas internas o de admin que YA validaron permisos arriba.
        return ejecutar(request, null, true);
    }

    /**
     * Crea una cuenta de ahorro validando que el socioId del request pertenezca
     * al usuario autenticado (anti-IDOR — issue #179).
     *
     * @param request datos de la cuenta
     * @param socioIdToken socioId extraído del JWT (puede ser {@code null} si {@code isAdmin})
     * @param isAdmin si el caller tiene rol ADMIN (puede crear cuenta para cualquier socio)
     * @throws AccessDeniedException si NO es admin y {@code request.socioId != socioIdToken}
     */
    @Transactional
    public CuentaAhorroResponse ejecutar(CreateCuentaAhorroRequest request,
                                         UUID socioIdToken,
                                         boolean isAdmin) {
        // Issue #179: anti-IDOR. Un socio solo puede crear cuentas para sí mismo.
        // Admin puede crear cuentas para cualquier socio (operación back-office).
        if (!isAdmin) {
            if (socioIdToken == null || !socioIdToken.equals(request.getSocioId())) {
                throw new AccessDeniedException(
                        "No tiene permisos para crear una cuenta a nombre de otro socio");
            }
        }

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