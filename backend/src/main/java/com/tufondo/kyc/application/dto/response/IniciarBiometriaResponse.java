package com.tufondo.kyc.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IniciarBiometriaResponse {
    private UUID intentoId;
    private String sessionId;
    /** URL del widget de Didit que el frontend debe abrir (iframe / nueva pestaña). */
    private String widgetUrl;
    /** Token corto-lived para autenticar el widget (puede ser null según el flow). */
    private String widgetToken;
    private String proveedor;
}
