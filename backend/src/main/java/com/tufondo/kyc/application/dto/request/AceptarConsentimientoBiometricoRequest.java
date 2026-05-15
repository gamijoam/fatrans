package com.tufondo.kyc.application.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AceptarConsentimientoBiometricoRequest {

    @AssertTrue(message = "Debe aceptar la política biométrica explícitamente")
    private boolean aceptado;

    @NotBlank(message = "La versión de la política es obligatoria")
    @Size(max = 20, message = "La versión no puede exceder 20 caracteres")
    private String versionPolitica;
}
