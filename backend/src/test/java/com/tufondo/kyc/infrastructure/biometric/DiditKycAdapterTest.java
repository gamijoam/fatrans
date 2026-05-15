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
        byte[] body = ("""
                {
                  "session_id": "%s",
                  "status": "APPROVED",
                  "decision": {
                    "liveness": { "score": 0.95, "attack_type": null },
                    "face_match": { "score": 0.91 },
                    "id_verification": { "confidence_score": 0.88 }
                  }
                }
                """.formatted(SESSION_ID)).getBytes(StandardCharsets.UTF_8);
        String signature = hmacHex(SECRET, body);
        String timestamp = String.valueOf(Instant.now().getEpochSecond());

        BiometricVerificatorPort.BiometricWebhookResult result =
                adapter.procesarWebhook(body, signature, timestamp);

        assertThat(result.sessionId()).isEqualTo(SESSION_ID);
        assertThat(result.outcome()).isEqualTo(BiometricVerificatorPort.BiometricOutcome.APROBADO);
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
        assertThatThrownBy(() -> adapter.procesarWebhook(body, null, "0"))
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
