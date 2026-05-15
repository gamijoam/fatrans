package com.tufondo.kyc.infrastructure.biometric;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
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

    /**
     * Procesa un webhook de Didit. Soporta DOS esquemas de firma:
     *
     * <ol>
     *   <li><b>X-Signature-V2</b> (preferido) — HMAC-SHA256 sobre el JSON
     *       <em>canonicalizado</em>: parse, ordenar keys recursivamente, convertir
     *       floats whole-number a enteros, re-encodear sin escape Unicode.
     *       Esta firma sobrevive a middleware que re-encoda JSON (Cloudflare).</li>
     *   <li><b>X-Signature</b> (legacy) — HMAC-SHA256 sobre el raw body literal.
     *       Frágil con caracteres especiales pero más rápida. Solo fallback.</li>
     * </ol>
     *
     * Si vienen ambos headers preferimos V2. Si la firma elegida no valida,
     * rechazamos sin caer al otro esquema (no aporta seguridad probar dos).
     */
    @Override
    public BiometricWebhookResult procesarWebhook(byte[] rawBody, String signatureHeader, String timestampHeader) {
        ensureEnabled();
        if (props.getWebhookSecret() == null || props.getWebhookSecret().isBlank()) {
            throw new KYCException("DIDIT_WEBHOOK_SECRET no configurado");
        }

        // Anti-replay: timestamp dentro de ventana (aplica a ambas firmas).
        validateTimestamp(timestampHeader);

        if (signatureHeader == null || signatureHeader.isBlank()) {
            throw new KYCException("Webhook sin firma: rechazado");
        }

        // El controller debe haber elegido el header correcto antes de delegar.
        // signatureHeader puede ser "v2:<hex>" o solo "<hex>" (raw). Convención
        // interna: si arranca con "v2:" usamos canonicalización V2; si no, simple.
        String expectedSignature;
        if (signatureHeader.startsWith("v2:")) {
            byte[] canonical;
            try {
                canonical = canonicalizeJson(rawBody);
            } catch (Exception e) {
                log.error("Webhook payload no es JSON parseable: {}", e.getMessage());
                throw new KYCException("Webhook payload inválido");
            }
            expectedSignature = hmacSha256Hex(props.getWebhookSecret(), canonical);
            String provided = normalizeSignature(signatureHeader.substring("v2:".length()));
            if (!constantTimeEquals(expectedSignature, provided)) {
                log.warn("Webhook Didit firma V2 inválida (timestamp={})", timestampHeader);
                throw new KYCException("Firma de webhook inválida");
            }
        } else {
            // Firma simple — HMAC sobre raw body literal.
            expectedSignature = hmacSha256Hex(props.getWebhookSecret(), rawBody);
            if (!constantTimeEquals(expectedSignature, normalizeSignature(signatureHeader))) {
                log.warn("Webhook Didit firma simple inválida (timestamp={})", timestampHeader);
                throw new KYCException("Firma de webhook inválida");
            }
        }

        try {
            JsonNode payload = objectMapper.readTree(rawBody);
            String sessionId = textOrNull(payload, "session_id");
            String status = textOrNull(payload, "status");
            JsonNode decision = payload.path("decision");

            BiometricOutcome outcome = mapStatus(status);
            // Didit envía resultados en sub-arrays (face_matches, liveness_checks,
            // id_verifications). Tomamos el primer elemento de cada uno.
            BigDecimal livenessScore = readArrayScore(decision, "liveness_checks", "score");
            BigDecimal faceMatchScore = readArrayScore(decision, "face_matches", "score");
            BigDecimal documentOcrScore = readArrayScore(decision, "id_verifications", "front_image_camera_front_face_match_score");

            String motivoFallo = null;
            String tipoAtaque = null;
            if (outcome == BiometricOutcome.RECHAZADO) {
                JsonNode firstLiveness = firstElement(decision.path("liveness_checks"));
                if (firstLiveness != null) {
                    tipoAtaque = textOrNull(firstLiveness, "method");
                    motivoFallo = textOrNull(firstLiveness, "warnings");
                }
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

    /**
     * Canonicaliza un payload JSON de Didit para validación X-Signature-V2:
     * <ul>
     *   <li>Parse JSON</li>
     *   <li>Para cada objeto: ordenar entries alfabéticamente por key, recursivo</li>
     *   <li>Para cada número: si es un float "whole number" (sin parte decimal),
     *       lo convertimos a {@link LongNode} para serializar como entero</li>
     *   <li>Re-encodear sin escape de Unicode (Jackson default) ni espacios</li>
     * </ul>
     */
    byte[] canonicalizeJson(byte[] rawBody) throws java.io.IOException {
        JsonNode root = objectMapper.readTree(rawBody);
        JsonNode canonical = canonicalizeNode(root);
        return objectMapper.writeValueAsBytes(canonical);
    }

    private JsonNode canonicalizeNode(JsonNode node) {
        if (node.isObject()) {
            ObjectNode sorted = JsonNodeFactory.instance.objectNode();
            List<String> names = new ArrayList<>();
            node.fieldNames().forEachRemaining(names::add);
            Collections.sort(names);
            for (String name : names) {
                sorted.set(name, canonicalizeNode(node.get(name)));
            }
            return sorted;
        }
        if (node.isArray()) {
            ArrayNode arr = JsonNodeFactory.instance.arrayNode();
            for (JsonNode child : node) {
                arr.add(canonicalizeNode(child));
            }
            return arr;
        }
        if (node.isFloatingPointNumber()) {
            double d = node.doubleValue();
            // Floats que son enteros se serializan como Long (Didit convention).
            if (!Double.isNaN(d) && !Double.isInfinite(d) && d == Math.floor(d)
                    && d >= Long.MIN_VALUE && d <= Long.MAX_VALUE) {
                return new LongNode((long) d);
            }
        }
        return node;
    }

    private JsonNode firstElement(JsonNode array) {
        if (array == null || !array.isArray() || array.size() == 0) return null;
        return array.get(0);
    }

    private BigDecimal readArrayScore(JsonNode parent, String arrayField, String scoreField) {
        if (parent == null) return null;
        JsonNode first = firstElement(parent.path(arrayField));
        if (first == null) return null;
        JsonNode v = first.path(scoreField);
        if (v.isMissingNode() || v.isNull() || !v.isNumber()) return null;
        double raw = v.asDouble();
        if (raw > 1.0) raw = raw / 100.0;  // normalizar a 0-1
        return BigDecimal.valueOf(raw);
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
