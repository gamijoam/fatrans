package com.tufondo.kyc.application.usecase;

import com.tufondo.kyc.domain.model.ConsentimientoBiometrico;
import com.tufondo.kyc.domain.repository.ConsentimientoBiometricoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Registra el consentimiento explícito y separado para tratamiento biométrico,
 * según exigencia LOPDP venezolana (datos biométricos = categoría sensible).
 *
 * El consentimiento general del KYC documental NO sirve — debe ser un consentimiento
 * informado, libre, expreso y específico para la biometría, mencionando el proveedor
 * y país de procesamiento.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrarConsentimientoBiometricoUseCase {

    /** Versión actual de la política biométrica. Bumpear cuando cambie el proveedor o el texto. */
    public static final String VERSION_POLITICA_ACTUAL = "1.0";
    public static final String PROVEEDOR_DEFAULT = "DIDIT_ES";
    public static final String PAIS_DEFAULT = "ES";

    private final ConsentimientoBiometricoRepository repository;

    @Transactional
    public ConsentimientoBiometrico ejecutar(UUID socioId, String versionPolitica,
                                             String ipCliente, String userAgent) {
        ConsentimientoBiometrico c = ConsentimientoBiometrico.builder()
                .socioId(socioId)
                .versionPolitica(versionPolitica != null ? versionPolitica : VERSION_POLITICA_ACTUAL)
                .proveedorDestino(PROVEEDOR_DEFAULT)
                .paisProcesamiento(PAIS_DEFAULT)
                .aceptado(true)
                .fechaConsentimiento(LocalDateTime.now())
                .ipCliente(ipCliente)
                .userAgent(userAgent)
                .build();
        ConsentimientoBiometrico guardado = repository.save(c);
        log.info("Consentimiento biométrico registrado. socioId={} version={}",
                socioId, guardado.getVersionPolitica());
        return guardado;
    }
}
