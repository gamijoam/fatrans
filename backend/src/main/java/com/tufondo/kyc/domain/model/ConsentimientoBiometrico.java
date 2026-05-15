package com.tufondo.kyc.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Consentimiento separado para tratamiento biométrico — exigido por LOPDP Venezuela
 * porque los datos biométricos son categoría sensible y deben tener:
 *  - Consentimiento explícito SEPARADO del consentimiento KYC documental.
 *  - Información sobre transferencia internacional (proveedor + país de procesamiento).
 *  - Trazabilidad (IP, user-agent, timestamp).
 *  - Versionado de política para que cambios en el texto requieran re-consentimiento.
 *  - Revocable (Art. 7 LOPDP).
 */
@Getter
@Builder
public class ConsentimientoBiometrico {

    private UUID id;
    private UUID socioId;

    private String versionPolitica;
    private String proveedorDestino;
    private String paisProcesamiento;

    private Boolean aceptado;
    private LocalDateTime fechaConsentimiento;
    private LocalDateTime fechaRevocacion;

    private String ipCliente;
    private String userAgent;

    private LocalDateTime createdAt;

    public boolean estaVigente() {
        return Boolean.TRUE.equals(aceptado) && fechaRevocacion == null;
    }

    public ConsentimientoBiometrico revocar() {
        return ConsentimientoBiometrico.builder()
                .id(this.id)
                .socioId(this.socioId)
                .versionPolitica(this.versionPolitica)
                .proveedorDestino(this.proveedorDestino)
                .paisProcesamiento(this.paisProcesamiento)
                .aceptado(this.aceptado)
                .fechaConsentimiento(this.fechaConsentimiento)
                .fechaRevocacion(LocalDateTime.now())
                .ipCliente(this.ipCliente)
                .userAgent(this.userAgent)
                .createdAt(this.createdAt)
                .build();
    }
}
