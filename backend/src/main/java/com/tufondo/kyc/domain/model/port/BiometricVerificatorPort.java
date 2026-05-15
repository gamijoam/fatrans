package com.tufondo.kyc.domain.model.port;

import java.math.BigDecimal;

/**
 * Puerto de verificación biométrica — abstrae al proveedor externo (Didit, AWS Rekognition,
 * etc.) para que el dominio pueda intercambiarlo sin tocar use cases ni entities.
 *
 * Cubre las tres capacidades que aplica el KYC biométrico de Fatrans:
 *  - Liveness detection (anti-spoofing): que la persona frente a la cámara sea real.
 *  - Face match: que la cara coincida con la foto de la cédula.
 *  - Identity document OCR: extracción de datos de la cédula (opcional, depende del flow).
 *
 * El proveedor maneja todo en una sola sesión asíncrona — creamos la sesión, el
 * usuario completa el flow en el widget del proveedor, recibimos el resultado por webhook.
 */
public interface BiometricVerificatorPort {

    /**
     * Crea una sesión nueva con el proveedor y devuelve los datos para que el
     * frontend monte el widget de captura.
     *
     * @param request socioId + email + flow type
     * @return session id + URL del widget + workflow id
     */
    BiometricSessionResponse iniciarSesion(BiometricSessionRequest request);

    /**
     * Verifica la firma del webhook (HMAC) y parsea el payload a un resultado normalizado.
     *
     * @param rawBody          body crudo recibido en el endpoint webhook
     * @param signatureHeader  header de firma (Didit usa "x-signature")
     * @param timestampHeader  timestamp del envío (anti-replay attack)
     * @return resultado normalizado o throw si la firma no valida
     */
    BiometricWebhookResult procesarWebhook(byte[] rawBody, String signatureHeader, String timestampHeader);

    /**
     * Solicita al proveedor que borre todos los datos asociados al session id.
     * Se invoca cuando el socio revoca su consentimiento biométrico (LOPDP Art. 7).
     */
    void solicitarBorradoSesion(String proveedorSessionId);

    String getProveedor();


    // ---- DTOs del puerto (records, no exponen detalles del proveedor) ----

    record BiometricSessionRequest(
            java.util.UUID socioId,
            String email,
            String nombreCompleto,
            String workflowId,
            String callbackUrl
    ) {}

    record BiometricSessionResponse(
            String sessionId,
            String workflowId,
            String widgetUrl,
            String widgetToken
    ) {}

    /**
     * Resultado parseado de un webhook del proveedor. El use case lo aplica
     * sobre el {@code VerificacionBiometrica} para hacer la transición de estado.
     */
    record BiometricWebhookResult(
            String sessionId,
            BiometricOutcome outcome,
            BigDecimal livenessScore,
            BigDecimal faceMatchScore,
            BigDecimal documentOcrScore,
            String motivoFallo,
            String tipoAtaqueDetectado
    ) {}

    enum BiometricOutcome { APROBADO, RECHAZADO, EN_PROGRESO, EXPIRADO, CANCELADO }
}
