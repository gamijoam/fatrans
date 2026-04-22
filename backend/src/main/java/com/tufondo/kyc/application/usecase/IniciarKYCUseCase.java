// com.tufondo.kyc.application.usecase.IniciarKYCUseCase
package com.tufondo.kyc.application.usecase;

import com.tufondo.kyc.application.dto.request.IniciarKYCRequest;
import com.tufondo.kyc.application.dto.response.IniciarKYCResponse;
import com.tufondo.kyc.domain.model.ConsentimientoKYC;
import com.tufondo.kyc.domain.model.VerificacionKYC;
import com.tufondo.kyc.domain.model.enums.EstadoVerificacion;
import com.tufondo.kyc.domain.model.enums.NivelVerificacion;
import com.tufondo.kyc.domain.model.enums.TipoDocumentoKYC;
import com.tufondo.kyc.domain.repository.ConsentimientoKYCRepository;
import com.tufondo.kyc.domain.repository.VerificacionKYCRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Use case para iniciar un proceso KYC.
 */
@Service
@RequiredArgsConstructor
public class IniciarKYCUseCase {

    private final VerificacionKYCRepository verificacionRepository;
    private final ConsentimientoKYCRepository consentimientoRepository;

    public IniciarKYCResponse ejecutar(IniciarKYCRequest request, UUID socioId) {

        // 1. Validar que no tiene KYC activo
        boolean tieneKYCActivo = verificacionRepository.existsBySocioIdAndEstadoIn(
            socioId,
            List.of(EstadoVerificacion.PENDIENTE, EstadoVerificacion.EN_REVISION, EstadoVerificacion.APROBADO)
        );

        if (tieneKYCActivo) {
            throw new com.tufondo.kyc.domain.exception.KYCYaExisteException("El socio ya tiene un proceso KYC activo");
        }

        // 2. Validar version de politica de privacidad
        List<String> versionesValidas = List.of("1.0", "2.0", "2.1");
        if (!versionesValidas.contains(request.getVersionPolitica())) {
            throw new com.tufondo.kyc.domain.exception.PoliticaPrivacidadInvalidaException(request.getVersionPolitica());
        }

        // 3. Guardar consentimiento (LOPDP)
        ConsentimientoKYC consentimiento = ConsentimientoKYC.builder()
            .socioId(socioId)
            .tipoConsentimiento("KYC_" + request.getNivel().name())
            .aceptado(request.getConsentimientoAceptado())
            .fechaConsentimiento(LocalDateTime.now())
            .ipCliente(request.getIpCliente())
            .userAgent(request.getUserAgent())
            .versionPolitica(request.getVersionPolitica())
            .build();

        consentimientoRepository.save(consentimiento);

        // 3. Crear verificacion KYC
        VerificacionKYC verificacion = VerificacionKYC.builder()
            .socioId(socioId)
            .nivel(request.getNivel())
            .estado(EstadoVerificacion.PENDIENTE)
            .fechaInicio(LocalDateTime.now())
            .fechaExpiracion(LocalDateTime.now().plusYears(2))
            .build();

        verificacion = verificacionRepository.save(verificacion);

        // 4. Retornar documentos requeridos segun nivel
        List<TipoDocumentoKYC> documentosRequeridos = getDocumentosRequeridos(request.getNivel());

        return IniciarKYCResponse.builder()
            .verificacionId(verificacion.getId())
            .nivel(verificacion.getNivel())
            .estado(verificacion.getEstado())
            .documentosRequeridos(documentosRequeridos)
            .mensaje("Proceso KYC iniciado. Por favor suba los documentos requeridos.")
            .build();
    }

    private List<TipoDocumentoKYC> getDocumentosRequeridos(NivelVerificacion nivel) {
        return switch (nivel) {
            case BASICO -> List.of(
                TipoDocumentoKYC.CEDULA_ANVERSO,
                TipoDocumentoKYC.CEDULA_REVERSO,
                TipoDocumentoKYC.SELFIE_CEDULA,
                TipoDocumentoKYC.COMPROBANTE_DOMICILIO
            );
            case MEDIO -> List.of(
                TipoDocumentoKYC.CEDULA_ANVERSO,
                TipoDocumentoKYC.CEDULA_REVERSO,
                TipoDocumentoKYC.SELFIE_CEDULA,
                TipoDocumentoKYC.COMPROBANTE_DOMICILIO,
                TipoDocumentoKYC.RIF_NIT,
                TipoDocumentoKYC.CONSTANCIA_TRABAJO
            );
            case COMPLETO -> List.of(TipoDocumentoKYC.values());
        };
    }
}