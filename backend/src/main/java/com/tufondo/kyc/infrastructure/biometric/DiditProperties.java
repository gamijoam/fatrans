package com.tufondo.kyc.infrastructure.biometric;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties de configuración del proveedor Didit (KYC biométrico).
 *
 * Se cargan desde env vars con prefijo {@code DIDIT_*} (mapeadas en application.yml):
 *
 *   DIDIT_API_KEY          → apiKey
 *   DIDIT_WEBHOOK_SECRET   → webhookSecret
 *   DIDIT_WORKFLOW_ID      → workflowId
 *   DIDIT_BASE_URL         → baseUrl (default: https://verification.didit.me/v2)
 *   DIDIT_CALLBACK_URL     → callbackUrl
 *   DIDIT_ENABLED          → enabled (default: false → adapter inactivo si falta config)
 *
 * Si {@code enabled} es false, el adapter inicia pero NO realiza llamadas reales —
 * permite que la app arranque en desarrollo sin credenciales.
 */
@Data
@ConfigurationProperties(prefix = "fatrans.kyc.didit")
public class DiditProperties {

    private boolean enabled = false;
    private String apiKey;
    private String webhookSecret;
    private String workflowId;
    private String baseUrl = "https://verification.didit.me/v2";
    private String callbackUrl;

    /** Ventana de tolerancia para webhook timestamp (anti-replay). 5 minutos por defecto. */
    private long webhookTimestampToleranceSeconds = 300;
}
