// com.tufondo.kyc.domain.model.ConsentimientoKYC
package com.tufondo.kyc.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad de dominio para ConsentimientoKYC.
 * Almacena el consentimiento del usuario para tratamiento de datos (LOPDP).
 */
@Getter
@Setter
@Builder
public class ConsentimientoKYC {

    private UUID id;
    private UUID socioId;
    private String tipoConsentimiento;
    private boolean aceptado;
    private LocalDateTime fechaConsentimiento;
    private String ipCliente;
    private String userAgent;
    private String versionPolitica;
}