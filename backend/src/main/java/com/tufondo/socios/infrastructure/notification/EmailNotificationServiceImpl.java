package com.tufondo.socios.infrastructure.notification;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * Implementación del servicio de email para notificaciones del flujo de registro.
 *
 * Dual-mode:
 *  - **Mock**: si `fatrans.mail.enabled=false` o no hay `JavaMailSender` configurado,
 *    loguea metadatos y sigue. Útil en dev, tests y en cualquier entorno donde
 *    aún no tengamos credenciales SMTP.
 *  - **Real**: si está habilitado, manda vía SMTP (Zoho por default). Falla
 *    silenciosamente al log si hay error de auth/red — no quiere romper el flujo
 *    de aprobación de un socio porque el SMTP esté caído momentáneamente. Si más
 *    adelante necesitamos confiabilidad (retry, cola), agregar Spring Retry o
 *    una cola dedicada.
 *
 * NUNCA logueamos la contraseña temporal en ningún caso (riesgo LOPDP).
 *
 * Templates: HTML inline en strings Java. Pragmático pero feo; cuando el set
 * de emails crezca, migrar a Thymeleaf (`spring-boot-starter-thymeleaf`) con
 * archivos en `src/main/resources/templates/email/`.
 */
@Service
@Slf4j
public class EmailNotificationServiceImpl implements EmailNotificationService {

    private final boolean enabled;
    private final JavaMailSender sender;
    private final String from;
    private final String fromName;
    private final String appBaseUrl;

    @Autowired
    public EmailNotificationServiceImpl(
            @Value("${fatrans.mail.enabled:false}") boolean enabled,
            @Autowired(required = false) JavaMailSender sender,
            @Value("${fatrans.mail.from:no-reply@fatrans.com.ve}") String from,
            @Value("${fatrans.mail.from-name:Fatrans}") String fromName,
            @Value("${fatrans.mail.app-base-url:https://app.fatrans.com.ve}") String appBaseUrl) {
        this.enabled = enabled;
        this.sender = sender;
        this.from = from;
        this.fromName = fromName;
        this.appBaseUrl = appBaseUrl;
        if (enabled && sender == null) {
            log.warn("fatrans.mail.enabled=true pero JavaMailSender no fue creado " +
                    "(faltan spring.mail.host/username?). Caigo a modo MOCK.");
        }
    }

    @Override
    public void enviarCredenciales(String email, String nombreUsuario, String passwordTemporal) {
        if (!isReal()) {
            // NUNCA logueamos passwordTemporal — solo metadatos para auditoría.
            log.info("Email de credenciales enviado (MOCK) a usuario={} (real desactivado o sin SMTP)",
                    nombreUsuario);
            return;
        }
        String html = """
                <p>Hola,</p>
                <p>Tu cuenta del <strong>Fondo de Ahorro Fatrans</strong> ya está activa.</p>
                <p><strong>Usuario:</strong> %s<br/>
                <strong>Contraseña temporal:</strong> %s</p>
                <p>Por seguridad, al ingresar por primera vez vas a tener que cambiar la
                contraseña.</p>
                <p style="margin-top: 20px;">
                  <a href="%s/login" style="background:#16A34A;color:#fff;padding:10px 20px;
                  text-decoration:none;border-radius:6px;display:inline-block;">
                    Iniciar sesión
                  </a>
                </p>
                <hr style="border:none;border-top:1px solid #ddd;margin:24px 0;"/>
                <p style="font-size:11px;color:#666;">
                  Si no solicitaste esta cuenta, ignorá este correo. Asociación de Ahorro y
                  Crédito Fatrans (RIF J-50516835-5).
                </p>
                """.formatted(escapeHtml(nombreUsuario), escapeHtml(passwordTemporal), appBaseUrl);

        boolean ok = sendHtml(email, "Tu cuenta del Fondo de Ahorro está activa", html);
        if (ok) {
            log.info("Email de credenciales enviado vía SMTP a usuario={}", nombreUsuario);
        }
    }

    @Override
    public void enviarNotificacionRechazo(String email, String motivo) {
        if (!isReal()) {
            log.info("Email de rechazo enviado (MOCK) a {}", email);
            return;
        }
        String html = """
                <p>Hola,</p>
                <p>Lamentamos informarte que tu <strong>solicitud de registro</strong> en el
                Fondo de Ahorro Fatrans no pudo ser aprobada.</p>
                <p><strong>Motivo:</strong> %s</p>
                <p>Si pensás que es un error o querés intentar de nuevo, escribinos a
                <a href="mailto:soporte@fatrans.com.ve">soporte@fatrans.com.ve</a> con tu
                cédula y los datos relevantes.</p>
                <hr style="border:none;border-top:1px solid #ddd;margin:24px 0;"/>
                <p style="font-size:11px;color:#666;">
                  Asociación de Ahorro y Crédito Fatrans (RIF J-50516835-5).
                </p>
                """.formatted(escapeHtml(motivo == null ? "(sin motivo especificado)" : motivo));

        boolean ok = sendHtml(email, "Tu solicitud de registro no fue aprobada", html);
        if (ok) {
            log.info("Email de rechazo enviado vía SMTP a {}", email);
        }
    }

    @Override
    public void enviarNotificacionSolicitudRecibida(String email) {
        if (!isReal()) {
            log.info("Email 'solicitud recibida' enviado (MOCK) a {}", email);
            return;
        }
        String html = """
                <p>Hola,</p>
                <p>Recibimos tu <strong>solicitud de registro</strong> en el Fondo de Ahorro
                Fatrans. Un administrador la va a revisar pronto.</p>
                <p>Cuando esté aprobada vas a recibir otro correo con tus credenciales de
                acceso.</p>
                <hr style="border:none;border-top:1px solid #ddd;margin:24px 0;"/>
                <p style="font-size:11px;color:#666;">
                  Asociación de Ahorro y Crédito Fatrans (RIF J-50516835-5).
                </p>
                """;

        boolean ok = sendHtml(email, "Recibimos tu solicitud de registro", html);
        if (ok) {
            log.info("Email 'solicitud recibida' enviado vía SMTP a {}", email);
        }
    }

    @Override
    public void enviarKycAprobado(String email, String nombreCompleto) {
        if (!isReal()) {
            log.info("Email 'KYC aprobado' enviado (MOCK) a {}", email);
            return;
        }
        String nombre = nombreCompleto == null || nombreCompleto.isBlank() ? "Hola" : "Hola " + escapeHtml(nombreCompleto);
        String html = """
                <p>%s,</p>
                <p>¡Buenas noticias! Tu <strong>verificación de identidad</strong> fue
                aprobada por nuestro equipo. Ya podés usar todos los servicios del
                Fondo de Ahorro Fatrans:</p>
                <ul>
                  <li>Depósitos y retiros sin límites adicionales</li>
                  <li>Solicitud de créditos</li>
                  <li>Gestión de beneficiarios</li>
                </ul>
                <p style="margin-top: 20px;">
                  <a href="%s/dashboard" style="background:#16A34A;color:#fff;padding:10px 20px;
                  text-decoration:none;border-radius:6px;display:inline-block;">
                    Ir a mi panel
                  </a>
                </p>
                <hr style="border:none;border-top:1px solid #ddd;margin:24px 0;"/>
                <p style="font-size:11px;color:#666;">
                  Asociación de Ahorro y Crédito Fatrans (RIF J-50516835-5).
                </p>
                """.formatted(nombre, appBaseUrl);

        boolean ok = sendHtml(email, "Tu identidad fue verificada — Fatrans", html);
        if (ok) {
            log.info("Email 'KYC aprobado' enviado vía SMTP a {}", email);
        }
    }

    @Override
    public void enviarKycRechazado(String email, String nombreCompleto, String motivo) {
        if (!isReal()) {
            log.info("Email 'KYC rechazado' enviado (MOCK) a {}", email);
            return;
        }
        String nombre = nombreCompleto == null || nombreCompleto.isBlank() ? "Hola" : "Hola " + escapeHtml(nombreCompleto);
        String textoMotivo = motivo == null || motivo.isBlank()
                ? "Revisá los detalles desde tu panel y volvé a enviar."
                : "Motivo: " + escapeHtml(motivo);
        String html = """
                <p>%s,</p>
                <p>Tu <strong>verificación de identidad</strong> no pasó la revisión.</p>
                <p>%s</p>
                <p>No te preocupes — podés volver a intentarlo. Asegurate de subir documentos
                legibles, completos y vigentes.</p>
                <p style="margin-top: 20px;">
                  <a href="%s/dashboard/kyc" style="background:#DC2626;color:#fff;padding:10px 20px;
                  text-decoration:none;border-radius:6px;display:inline-block;">
                    Reintentar verificación
                  </a>
                </p>
                <hr style="border:none;border-top:1px solid #ddd;margin:24px 0;"/>
                <p style="font-size:11px;color:#666;">
                  Si tenés dudas, escribinos a
                  <a href="mailto:soporte@fatrans.com.ve">soporte@fatrans.com.ve</a>.
                  Asociación de Ahorro y Crédito Fatrans (RIF J-50516835-5).
                </p>
                """.formatted(nombre, textoMotivo, appBaseUrl);

        boolean ok = sendHtml(email, "Tu verificación de identidad no pasó la revisión — Fatrans", html);
        if (ok) {
            log.info("Email 'KYC rechazado' enviado vía SMTP a {}", email);
        }
    }

    @Override
    public void enviarKycRequiereInfo(String email, String nombreCompleto, String detalle) {
        if (!isReal()) {
            log.info("Email 'KYC requiere info' enviado (MOCK) a {}", email);
            return;
        }
        String nombre = nombreCompleto == null || nombreCompleto.isBlank() ? "Hola" : "Hola " + escapeHtml(nombreCompleto);
        String textoDetalle = detalle == null || detalle.isBlank()
                ? "El analista necesita información adicional para continuar."
                : "El analista necesita lo siguiente: " + escapeHtml(detalle);
        String html = """
                <p>%s,</p>
                <p>Estamos revisando tu <strong>verificación de identidad</strong> y necesitamos
                un poco más de información.</p>
                <p>%s</p>
                <p style="margin-top: 20px;">
                  <a href="%s/dashboard/kyc" style="background:#2563EB;color:#fff;padding:10px 20px;
                  text-decoration:none;border-radius:6px;display:inline-block;">
                    Completar verificación
                  </a>
                </p>
                <hr style="border:none;border-top:1px solid #ddd;margin:24px 0;"/>
                <p style="font-size:11px;color:#666;">
                  Asociación de Ahorro y Crédito Fatrans (RIF J-50516835-5).
                </p>
                """.formatted(nombre, textoDetalle, appBaseUrl);

        boolean ok = sendHtml(email, "Necesitamos más información para tu verificación — Fatrans", html);
        if (ok) {
            log.info("Email 'KYC requiere info' enviado vía SMTP a {}", email);
        }
    }

    // ────────────────────────────────────────────────────────
    //  Helpers privados
    // ────────────────────────────────────────────────────────

    /** Sólo manda vía SMTP si está habilitado Y el sender se construyó. */
    private boolean isReal() {
        return enabled && sender != null;
    }

    /**
     * Envía un email HTML. Devuelve true si fue OK, false si falló. NO propaga
     * la excepción — el caller no se entera y el flujo de aprobación sigue su
     * curso (preferible vs. abortar la aprobación de un socio porque SMTP cayó).
     * Para reintentos confiables: integrar Spring Retry o una cola dedicada.
     */
    private boolean sendHtml(String to, String subject, String htmlBody) {
        try {
            MimeMessage mime = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, StandardCharsets.UTF_8.name());
            try {
                helper.setFrom(new InternetAddress(from, fromName, StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                helper.setFrom(from);
            }
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            sender.send(mime);
            return true;
        } catch (MailException | MessagingException e) {
            log.error("Fallo enviando email a {} (asunto='{}'): {}", to, subject, e.getMessage());
            return false;
        }
    }

    /** Sanitización mínima de entidades HTML para evitar XSS en valores
        dinámicos (nombre de usuario, motivo de rechazo). */
    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
