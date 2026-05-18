package com.tufondo.kyc.infrastructure.biometric;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tufondo.kyc.domain.exception.KYCException;
import com.tufondo.kyc.domain.model.port.BiometricVerificatorPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests críticos del adapter Didit centrados en la verificación del webhook (HMAC + ventana
 * de timestamp). Si el adapter aceptara un webhook con firma inválida, un atacante podría
 * marcar manualmente una verificación como APROBADA y burlar todo el flujo KYC.
 *
 * No tocamos la red real — los métodos {@code iniciarSesion} y {@code solicitarBorradoSesion}
 * requieren e2e con Didit sandbox y se prueban en otro escenario.
 */
@DisplayName("DiditKycAdapter — verificación de webhook (HMAC + anti-replay)")
class DiditKycAdapterTest {

    private static final String SECRET = "test-secret-for-hmac-256";
    private static final String SESSION_ID = "didit-sess-abc-123";

    private DiditKycAdapter adapter;

    @BeforeEach
    void setUp() {
        DiditProperties props = new DiditProperties();
        props.setEnabled(true);
        props.setApiKey("test-api-key");
        props.setWebhookSecret(SECRET);
        props.setWorkflowId("test-workflow");
        props.setWebhookTimestampToleranceSeconds(300);
        adapter = new DiditKycAdapter(props, new ObjectMapper());
    }

    @Test
    @DisplayName("Acepta un webhook con firma HMAC correcta y timestamp dentro de ventana")
    void aceptaWebhookValido() {
        // Payload con el formato real que Didit v3 envía: scores dentro de
        // arrays `liveness_checks` / `face_matches`. El test original usaba
        // un formato viejo con `liveness` objeto, que dejaba los scores en
        // null y rompía las aserciones.
        byte[] body = ("""
                {
                  "session_id": "%s",
                  "status": "APPROVED",
                  "decision": {
                    "liveness_checks": [{"score": 95.0, "status": "Approved", "method": "FLASHING"}],
                    "face_matches": [{"score": 91.0, "status": "Approved"}],
                    "id_verifications": [{"front_image_camera_front_face_match_score": 88.0}]
                  }
                }
                """.formatted(SESSION_ID)).getBytes(StandardCharsets.UTF_8);
        String signature = hmacHex(SECRET, body);
        String timestamp = String.valueOf(Instant.now().getEpochSecond());

        BiometricVerificatorPort.BiometricWebhookResult result =
                adapter.procesarWebhook(body, signature, timestamp);

        assertThat(result.sessionId()).isEqualTo(SESSION_ID);
        assertThat(result.outcome()).isEqualTo(BiometricVerificatorPort.BiometricOutcome.APROBADO);
        // Adapter normaliza scores 0-100 a 0-1.
        assertThat(result.livenessScore()).isEqualByComparingTo("0.95");
        assertThat(result.faceMatchScore()).isEqualByComparingTo("0.91");
    }

    @Test
    @DisplayName("Rechaza un webhook con firma HMAC inválida (intento de spoofing)")
    void rechazaFirmaInvalida() {
        byte[] body = ("{\"session_id\":\"" + SESSION_ID + "\",\"status\":\"APPROVED\"}").getBytes();
        String wrongSignature = hmacHex("otro-secret", body);
        String timestamp = String.valueOf(Instant.now().getEpochSecond());

        assertThatThrownBy(() -> adapter.procesarWebhook(body, wrongSignature, timestamp))
                .isInstanceOf(KYCException.class)
                .hasMessageContaining("Firma de webhook inválida");
    }

    @Test
    @DisplayName("Rechaza un webhook con timestamp fuera de ventana (replay attack)")
    void rechazaTimestampViejo() {
        byte[] body = ("{\"session_id\":\"" + SESSION_ID + "\",\"status\":\"APPROVED\"}").getBytes();
        String signature = hmacHex(SECRET, body);
        // 1 hora atrás → fuera de la ventana de 300s.
        String timestamp = String.valueOf(Instant.now().getEpochSecond() - 3600);

        assertThatThrownBy(() -> adapter.procesarWebhook(body, signature, timestamp))
                .isInstanceOf(KYCException.class)
                .hasMessageContaining("ventana temporal");
    }

    @Test
    @DisplayName("Rechaza un webhook sin firma")
    void rechazaSinFirma() {
        byte[] body = "{}".getBytes();
        // Usamos timestamp ACTUAL para que pase la validación temporal y
        // así el adapter llegue al check "sin firma". Antes usábamos "0"
        // (epoch 1970) que disparaba el error de ventana temporal primero
        // y enmascaraba el caso que queremos verificar.
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        assertThatThrownBy(() -> adapter.procesarWebhook(body, null, timestamp))
                .isInstanceOf(KYCException.class)
                .hasMessageContaining("sin firma");
    }

    @Test
    @DisplayName("Acepta firma con prefijo 'sha256=' (formato alterno común)")
    void aceptaFirmaConPrefijoSha256() {
        byte[] body = ("{\"session_id\":\"" + SESSION_ID + "\",\"status\":\"DECLINED\"}").getBytes();
        String signature = "sha256=" + hmacHex(SECRET, body);
        String timestamp = String.valueOf(Instant.now().getEpochSecond());

        BiometricVerificatorPort.BiometricWebhookResult result =
                adapter.procesarWebhook(body, signature, timestamp);

        assertThat(result.outcome()).isEqualTo(BiometricVerificatorPort.BiometricOutcome.RECHAZADO);
    }

    @Test
    @DisplayName("Acepta firma X-Signature-V2: HMAC sobre JSON canonicalizado (sort keys + whole-number floats)")
    void aceptaFirmaV2Canonicalizada() throws Exception {
        // Payload con claves desordenadas y un float whole-number (1641038400.0).
        // El raw body NO tiene el formato canónico — Didit firma el canónico.
        byte[] rawBody = """
                {
                  "status": "Approved",
                  "session_id": "%s",
                  "timestamp": 1641038400.0,
                  "decision": {
                    "session_id": "%s",
                    "status": "Approved",
                    "face_matches": [{"score": 95.26, "status": "Approved"}],
                    "liveness_checks": [{"score": 99.03, "status": "Approved", "method": "FLASHING"}]
                  }
                }
                """.formatted(SESSION_ID, SESSION_ID).getBytes(StandardCharsets.UTF_8);

        // Canonicalizar manualmente igual que el adapter, firmar el resultado.
        byte[] canonical = adapter.canonicalizeJson(rawBody);
        String signature = "v2:" + hmacHex(SECRET, canonical);
        String timestamp = String.valueOf(Instant.now().getEpochSecond());

        BiometricVerificatorPort.BiometricWebhookResult result =
                adapter.procesarWebhook(rawBody, signature, timestamp);

        assertThat(result.sessionId()).isEqualTo(SESSION_ID);
        assertThat(result.outcome()).isEqualTo(BiometricVerificatorPort.BiometricOutcome.APROBADO);
        // Scores se leen de los arrays anidados — normalizan de 0-100 a 0-1.
        assertThat(result.faceMatchScore()).isEqualByComparingTo("0.9526");
        assertThat(result.livenessScore()).isEqualByComparingTo("0.9903");
    }

    @Test
    @DisplayName("Rechaza firma V2 cuando la firma fue calculada con otro secreto")
    void rechazaFirmaV2Invalida() {
        // Usamos un payload simple cuya canonicalización SÍ difiere del raw
        // (claves desordenadas + número whole float) y firmamos con OTRO
        // secreto. Cualquiera de las dos diferencias por sí sola hace que la
        // firma no matchee, garantizando rechazo. El test anterior firmaba
        // el raw body, pero como su payload era ya canónico, los bytes
        // canonical y raw eran idénticos y la firma matcheaba.
        byte[] rawBody = ("{\"status\":\"Approved\",\"session_id\":\"" + SESSION_ID + "\",\"score\":1.0}")
                .getBytes(StandardCharsets.UTF_8);
        String signature = "v2:" + hmacHex("OTRO-SECRETO", rawBody);
        String timestamp = String.valueOf(Instant.now().getEpochSecond());

        assertThatThrownBy(() -> adapter.procesarWebhook(rawBody, signature, timestamp))
                .isInstanceOf(KYCException.class)
                .hasMessageContaining("Firma de webhook inválida");
    }

    @Test
    @DisplayName("Canonicalización: keys se ordenan alfabéticamente y whole-number floats se serializan como int")
    void canonicalizacionCorrecta() throws Exception {
        byte[] input = "{\"b\":2,\"a\":1.0,\"c\":{\"y\":\"x\",\"x\":3.5}}"
                .getBytes(StandardCharsets.UTF_8);
        byte[] output = adapter.canonicalizeJson(input);
        String result = new String(output, StandardCharsets.UTF_8);

        // Keys ordenadas alfabéticamente (a, b, c) y dentro de c (x, y).
        // 1.0 → 1 (whole-number a Long). 3.5 → 3.5 (mantiene float).
        assertThat(result).isEqualTo("{\"a\":1,\"b\":2,\"c\":{\"x\":3.5,\"y\":\"x\"}}");
    }

    /** Helper local: computa HMAC-SHA256 en hex (igual lógica que el adapter). */
    private String hmacHex(String secret, byte[] body) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(body));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
