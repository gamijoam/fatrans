// com.tufondo.kyc.application.usecase.RevocarConsentimientoUseCase
package com.tufondo.kyc.application.usecase;

import com.tufondo.kyc.application.dto.request.RevocarConsentimientoRequest;
import com.tufondo.kyc.application.dto.response.RevocarConsentimientoResponse;
import com.tufondo.kyc.domain.exception.ConsentimientoNoEncontradoException;
import com.tufondo.kyc.domain.model.ConsentimientoKYC;
import com.tufondo.kyc.domain.repository.ConsentimientoKYCRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Use case para revocar consentimiento KYC (LOPDP Art. 7).
 * Implementa el derecho del titular de datos a revocar el consentimiento.
 */
@Service
@RequiredArgsConstructor
public class RevocarConsentimientoUseCase {

    private final ConsentimientoKYCRepository consentimientoRepository;

    /**
     * Revoca el consentimiento activo de un socio.
     * Según LOPDP Art. 7, el consentimiento puede ser revocado en cualquier momento.
     *
     * @param request datos de la solicitud de revocación
     * @param socioId ID del socio que revoca
     * @param ipCliente IP del cliente para auditoría
     * @param userAgent User-Agent para auditoría
     * @return respuesta con confirmación de revocación
     */
    public RevocarConsentimientoResponse ejecutar(RevocarConsentimientoRequest request,
                                                   UUID socioId,
                                                   String ipCliente,
                                                   String userAgent) {
        // 1. Obtener consentimiento activo
        ConsentimientoKYC consentimiento = consentimientoRepository.findActiveBySocioId(socioId)
            .orElseThrow(() -> new ConsentimientoNoEncontradoException(socioId.toString()));

        // 2. Validar confirmación
        if (request.getConfirmacion() == null || !request.getConfirmacion()) {
            return RevocarConsentimientoResponse.builder()
                .consentimientoId(consentimiento.getId())
                .mensaje("Se requiere confirmación expresa para revocar el consentimiento")
                .fechaRevocacion(null)
                .revocacionExitosa(false)
                .build();
        }

        // 3. Crear nuevo registro de revocación para auditoría LOPDP
        // Se mantiene el historial en lugar de modificar el registro original
        ConsentimientoKYC revocacion = ConsentimientoKYC.builder()
            .socioId(socioId)
            .tipoConsentimiento("REVOCACION")
            .aceptado(false)
            .fechaConsentimiento(LocalDateTime.now())
            .ipCliente(ipCliente)
            .userAgent(userAgent)
            .versionPolitica(consentimiento.getVersionPolitica())
            .build();

        revocacion = consentimientoRepository.save(revocacion);

        // 4. Retornar respuesta
        return RevocarConsentimientoResponse.builder()
            .consentimientoId(revocacion.getId())
            .mensaje("Su consentimiento ha sido revocado exitosamente. " +
                    "Según LOPDP Art. 7, el tratamiento de sus datos ha cesado.")
            .fechaRevocacion(LocalDateTime.now())
            .revocacionExitosa(true)
            .build();
    }
}