package com.tufondo.kyc.infrastructure.biometric;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tufondo.kyc.domain.exception.KYCException;
import com.tufondo.kyc.domain.model.port.BiometricVerificatorPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Adapter del puerto {@link BiometricVerificatorPort} para el proveedor Didit
 * (didit.me) — passive liveness iBeta L1 + face match + ID document OCR + AML,
 * gratis hasta 500 verificaciones/mes.
 *
 * Documentación API: https://docs.didit.me
 *
 * Capacidades:
 *  - {@link #iniciarSesion}: crea una sesión en Didit y devuelve la URL del widget
 *    que el frontend embeberá. El widget se encarga de la captura de cámara,
 *    instrucciones al usuario, captura de cédula y selfie con liveness.
 *  - {@link #procesarWebhook}: verifica la firma HMAC-SHA256 + ventana de timestamp
 *    (anti-replay) y normaliza el payload a {@link BiometricWebhookResult}.
 *  - {@link #solicitarBorradoSesion}: para revocación LOPDP — pide a Didit que
 *    elimine los datos asociados al session id.
 *
 * Si {@code fatrans.kyc.didit.enabled=false} (caso desarrollo sin credenciales),
 * lanza {@link KYCException} explícita en lugar de hacer llamadas con credenciales nulas.
 */
@Service
@Slf4j
@Configuration
@EnableConfigurationProperties(DiditProperties.class)
public class DiditKycAdapter implements BiometricVerificatorPort {

    public static final String PROVIDER_NAME = "DIDIT";

    private final DiditProperties props;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public DiditKycAdapter(DiditProperties props, ObjectMapper objectMapper) {
        this.props = props;
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    @Override
    public String getProveedor() {
        return PROVIDER_NAME;
    }

    @Override
    public BiometricSessionResponse iniciarSesion(BiometricSessionRequest request) {
        ensureEnabled();

        String workflowId = request.workflowId() != null && !request.workflowId().isBlank()
                ? request.workflowId()
                : props.getWorkflowId();
        if (workflowId == null || workflowId.isBlank()) {
            throw new KYCException("Didit workflow_id no configurado");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("workflow_id", workflowId);
        body.put("vendor_data", request.socioId().toString());
        // Didit no requiere el email; solo lo agregamos como metadata para el dashboard.
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("email", request.email());
        metadata.put("nombre_completo", request.nombreCompleto());
        body.put("metadata", metadata);

        String callback = request.callbackUrl() != null && !request.callbackUrl().isBlank()
                ? request.callbackUrl()
                : props.getCallbackUrl();
        if (callback != null && !callback.isBlank()) {
            body.put("callback", callback);
        }

        HttpHeaders headers = baseHeaders();
        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    props.getBaseUrl() + "/session/",
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    JsonNode.class
            );
            JsonNode payload = response.getBody();
            if (payload == null) {
                throw new KYCException("Didit devolvió payload vacío al crear sesión");
            }
            String sessionId = textOrNull(payload, "session_id");
            String widgetUrl = textOrNull(payload, "url");
            String widgetToken = textOrNull(payload, "session_token");
            if (sessionId == null || widgetUrl == null) {
                throw new KYCException("Respuesta de Didit incompleta: falta session_id o url");
            }
            log.info("Didit session created. socioId={} sessionId={}", request.socioId(), sessionId);
            return new BiometricSessionResponse(sessionId, workflowId, widgetUrl, widgetToken);
        } catch (RestClientException e) {
            // No logueamos el body ni headers (puede contener apiKey reflejada en algunos errores).
            log.error("Didit session error: {}", e.getMessage());
            throw new KYCException("Error iniciando sesión biométrica con el proveedor");
        }
    }

    @Override
    public BiometricWebhookResult procesarWebhook(byte[] rawBody, String signatureHeader, String timestampHeader) {
        ensureEnabled();

        if (signatureHeader == null || signatureHeader.isBlank()) {
            throw new KYCException("Webhook sin firma: rechazado");
        }
        if (props.getWebhookSecret() == null || props.getWebhookSecret().isBlank()) {
            throw new KYCException("DIDIT_WEBHOOK_SECRET no configurado");
        }

        // Anti-replay: el timestamp debe estar dentro de la ventana de tolerancia.
        validateTimestamp(timestampHeader);

        // Verificación HMAC-SHA256 del body crudo con la clave compartida.
        String expectedSignature = hmacSha256Hex(props.getWebhookSecret(), rawBody);
        if (!constantTimeEquals(expectedSignature, normalizeSignature(signatureHeader))) {
            log.warn("Webhook Didit firma inválida (timestamp={})", timestampHeader);
            throw new KYCException("Firma de webhook inválida");
        }

        try {
            JsonNode payload = objectMapper.readTree(rawBody);
            String sessionId = textOrNull(payload, "session_id");
            String status = textOrNull(payload, "status");
            JsonNode decision = payload.path("decision");

            BiometricOutcome outcome = mapStatus(status);
            BigDecimal livenessScore = readScore(decision, "liveness", "score");
            BigDecimal faceMatchScore = readScore(decision, "face_match", "score");
            BigDecimal documentOcrScore = readScore(decision, "id_verification", "confidence_score");

            String motivoFallo = null;
            String tipoAtaque = null;
            if (outcome == BiometricOutcome.RECHAZADO) {
                motivoFallo = textOrNull(decision, "warnings");
                tipoAtaque = textOrNull(decision.path("liveness"), "attack_type");
            }

            return new BiometricWebhookResult(
                    sessionId, outcome, livenessScore, faceMatchScore, documentOcrScore,
                    motivoFallo, tipoAtaque
            );
        } catch (Exception e) {
            log.error("Error parseando webhook Didit: {}", e.getMessage());
            throw new KYCException("Webhook payload inválido");
        }
    }

    @Override
    public void solicitarBorradoSesion(String proveedorSessionId) {
        ensureEnabled();
        try {
            HttpHeaders headers = baseHeaders();
            restTemplate.exchange(
                    props.getBaseUrl() + "/session/" + proveedorSessionId + "/",
                    HttpMethod.DELETE,
                    new HttpEntity<>(headers),
                    Void.class
            );
            log.info("Didit session borrada (revocación LOPDP). sessionId={}", proveedorSessionId);
        } catch (RestClientException e) {
            log.error("Error al solicitar borrado de sesión Didit {}: {}", proveedorSessionId, e.getMessage());
            throw new KYCException("No se pudo solicitar el borrado en el proveedor");
        }
    }

    // ---------- helpers internos ----------

    private void ensureEnabled() {
        if (!props.isEnabled()) {
            throw new KYCException("KYC biométrico (Didit) no está habilitado en este entorno");
        }
    }

    private HttpHeaders baseHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", props.getApiKey());
        return headers;
    }

    private void validateTimestamp(String timestampHeader) {
        if (timestampHeader == null || timestampHeader.isBlank()) {
            // Algunos proveedores no lo envían — toleramos pero registramos.
            log.debug("Webhook Didit sin timestamp; saltando validación anti-replay");
            return;
        }
        try {
            long ts = Long.parseLong(timestampHeader);
            long now = Instant.now().getEpochSecond();
            long delta = Math.abs(now - ts);
            if (delta > props.getWebhookTimestampToleranceSeconds()) {
                throw new KYCException("Webhook fuera de ventana temporal (replay sospechoso)");
            }
        } catch (NumberFormatException e) {
            throw new KYCException("Timestamp de webhook inválido");
        }
    }

    private String hmacSha256Hex(String secret, byte[] body) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(body);
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new KYCException("Error computando HMAC-SHA256 del webhook");
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) result |= a.charAt(i) ^ b.charAt(i);
        return result == 0;
    }

    private String normalizeSignature(String header) {
        // Didit suele enviar la firma como hex puro; algunos proveedores anteponen "sha256=".
        return header.toLowerCase().startsWith("sha256=") ? header.substring(7) : header;
    }

    private BiometricOutcome mapStatus(String status) {
        if (status == null) return BiometricOutcome.EN_PROGRESO;
        return switch (status.toUpperCase()) {
            case "APPROVED", "VERIFIED" -> BiometricOutcome.APROBADO;
            case "DECLINED", "REJECTED" -> BiometricOutcome.RECHAZADO;
            case "EXPIRED" -> BiometricOutcome.EXPIRADO;
            case "ABANDONED", "CANCELLED" -> BiometricOutcome.CANCELADO;
            default -> BiometricOutcome.EN_PROGRESO;
        };
    }

    private String textOrNull(JsonNode node, String field) {
        if (node == null) return null;
        JsonNode v = node.path(field);
        return v.isMissingNode() || v.isNull() ? null : v.asText();
    }

    private BigDecimal readScore(JsonNode parent, String section, String field) {
        if (parent == null) return null;
        JsonNode v = parent.path(section).path(field);
        if (v.isMissingNode() || v.isNull() || !v.isNumber()) return null;
        // Algunos endpoints devuelven 0-100, otros 0-1. Normalizamos a 0-1.
        double raw = v.asDouble();
        if (raw > 1.0) raw = raw / 100.0;
        return BigDecimal.valueOf(raw);
    }
}
