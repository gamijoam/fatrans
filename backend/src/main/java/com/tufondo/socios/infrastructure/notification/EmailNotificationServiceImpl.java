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
        String preheader = "Tu cuenta del Fondo de Ahorro ya está activa. Cambia tu contraseña al primer ingreso.";
        String contenido = """
                <h1 style="margin:0 0 16px;font-size:22px;color:#0F2744;">¡Bienvenido al Fondo de Ahorro!</h1>
                <p style="margin:0 0 16px;color:#475569;line-height:1.5;">
                  Tu solicitud de registro fue aprobada. A continuación tus credenciales de acceso —
                  guardalas en un lugar seguro.
                </p>
                <table cellpadding="0" cellspacing="0" border="0" role="presentation"
                  style="width:100%%;border-collapse:separate;border-spacing:0;border:1px solid #e2e8f0;
                  border-radius:8px;background:#f8fafc;margin:8px 0 20px;">
                  <tr>
                    <td style="padding:14px 18px;border-bottom:1px solid #e2e8f0;
                      font-family:'SF Mono','Consolas',monospace;font-size:14px;">
                      <span style="color:#64748b;font-size:11px;text-transform:uppercase;
                        letter-spacing:0.5px;display:block;margin-bottom:4px;">Usuario</span>
                      <strong style="color:#0F2744;font-size:16px;">%s</strong>
                    </td>
                  </tr>
                  <tr>
                    <td style="padding:14px 18px;
                      font-family:'SF Mono','Consolas',monospace;font-size:14px;">
                      <span style="color:#64748b;font-size:11px;text-transform:uppercase;
                        letter-spacing:0.5px;display:block;margin-bottom:4px;">Contraseña temporal</span>
                      <strong style="color:#0F2744;font-size:16px;">%s</strong>
                    </td>
                  </tr>
                </table>
                <p style="margin:0 0 24px;color:#475569;line-height:1.5;font-size:14px;">
                  <strong>Importante:</strong> por seguridad, al ingresar por primera vez vamos a
                  pedirte que cambies esta contraseña por una propia.
                </p>
                """.formatted(escapeHtml(nombreUsuario), escapeHtml(passwordTemporal));
        String html = renderEmail(preheader, contenido, "Iniciar sesión", appBaseUrl + "/login");
        boolean ok = sendHtml(email, "Tu cuenta del Fondo de Ahorro Fatrans está activa", html);
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
        String preheader = "Tu solicitud de registro no fue aprobada.";
        String motivoSeguro = escapeHtml(motivo == null || motivo.isBlank() ? "(sin motivo especificado)" : motivo);
        String contenido = """
                <h1 style="margin:0 0 16px;font-size:22px;color:#0F2744;">Sobre tu solicitud</h1>
                <p style="margin:0 0 16px;color:#475569;line-height:1.5;">
                  Lamentamos informarte que tu solicitud de registro en el Fondo de Ahorro Fatrans
                  no pudo ser aprobada en esta oportunidad.
                </p>
                <div style="background:#fef2f2;border-left:4px solid #ef4444;padding:14px 16px;
                  border-radius:4px;margin:16px 0 24px;">
                  <p style="margin:0 0 6px;color:#991b1b;font-size:12px;font-weight:600;
                    text-transform:uppercase;letter-spacing:0.5px;">Motivo</p>
                  <p style="margin:0;color:#7f1d1d;line-height:1.5;">%s</p>
                </div>
                <p style="margin:0 0 8px;color:#475569;line-height:1.5;font-size:14px;">
                  Si pensás que es un error o querés intentar nuevamente, escribinos a
                  <a href="mailto:soporte@fatrans.com.ve" style="color:#16A34A;font-weight:600;
                    text-decoration:none;">soporte@fatrans.com.ve</a>
                  con tu cédula y los datos relevantes.
                </p>
                """.formatted(motivoSeguro);
        String html = renderEmail(preheader, contenido, null, null);
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
        String preheader = "Recibimos tu solicitud de registro. Te avisaremos cuando esté aprobada.";
        String contenido = """
                <h1 style="margin:0 0 16px;font-size:22px;color:#0F2744;">Recibimos tu solicitud</h1>
                <p style="margin:0 0 16px;color:#475569;line-height:1.5;">
                  Gracias por solicitar tu registro en el <strong>Fondo de Ahorro Fatrans</strong>.
                  Un administrador revisará tu solicitud en las próximas horas.
                </p>
                <p style="margin:0 0 24px;color:#475569;line-height:1.5;">
                  Cuando esté aprobada vas a recibir otro correo con tus credenciales de acceso.
                  No necesitás hacer nada más por ahora.
                </p>
                """;
        String html = renderEmail(preheader, contenido, null, null);
        boolean ok = sendHtml(email, "Recibimos tu solicitud de registro · Fatrans", html);
        if (ok) {
            log.info("Email 'solicitud recibida' enviado vía SMTP a {}", email);
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
     * Wrapper común para todos los emails: header con logo + contenido + CTA
     * opcional + footer institucional.
     *
     * HTML cuidadosamente compatible con clientes problemáticos (Outlook,
     * Gmail strip estilos, etc): solo `<table>` para layout, estilos INLINE
     * (las `<style>` tag se ignoran en muchos clientes), sin imágenes
     * críticas (las imágenes pueden no cargarse — el logo es decorativo,
     * no transporta info).
     *
     * El logo se sirve desde `MAIL_APP_BASE_URL + /logo-fatrans.png`. Si el
     * cliente bloquea imágenes, ven el `alt="Fatrans"` y el header sigue
     * legible gracias al título textual debajo.
     *
     * `preheader` es texto invisible al principio del mail que los clientes
     * muestran como preview en la bandeja de entrada (Gmail, Apple Mail).
     * Truco estándar de email marketing.
     */
    private String renderEmail(String preheader, String contenidoHtml, String ctaText, String ctaUrl) {
        String logoUrl = appBaseUrl + "/logo-fatrans.png";
        String ctaHtml = "";
        if (ctaText != null && ctaUrl != null) {
            ctaHtml = """
                    <table cellpadding="0" cellspacing="0" border="0" role="presentation"
                      style="margin:0 0 24px;">
                      <tr>
                        <td style="background:#16A34A;border-radius:8px;">
                          <a href="%s" style="display:inline-block;padding:12px 28px;
                            font-family:Arial,Helvetica,sans-serif;font-size:15px;font-weight:600;
                            color:#ffffff;text-decoration:none;">
                            %s →
                          </a>
                        </td>
                      </tr>
                    </table>
                    """.formatted(ctaUrl, escapeHtml(ctaText));
        }

        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                  <meta charset="UTF-8"/>
                  <meta name="viewport" content="width=device-width,initial-scale=1.0"/>
                  <title>Fatrans</title>
                </head>
                <body style="margin:0;padding:0;background:#f1f5f9;
                  font-family:'Segoe UI',Tahoma,Geneva,Verdana,Arial,sans-serif;
                  color:#0F2744;">
                  <span style="display:none !important;visibility:hidden;opacity:0;color:transparent;
                    height:0;max-height:0;overflow:hidden;mso-hide:all;">%s</span>
                  <table cellpadding="0" cellspacing="0" border="0" role="presentation"
                    style="width:100%%;background:#f1f5f9;">
                    <tr>
                      <td align="center" style="padding:24px 12px;">
                        <table cellpadding="0" cellspacing="0" border="0" role="presentation"
                          style="width:100%%;max-width:560px;background:#ffffff;
                          border-radius:12px;overflow:hidden;
                          box-shadow:0 1px 3px rgba(15,39,68,0.08);">
                          <tr>
                            <td style="background:#0F2744;padding:24px;text-align:center;">
                              <img src="%s" alt="Fatrans" width="48" height="48"
                                style="display:inline-block;border:0;outline:none;
                                text-decoration:none;width:48px;height:48px;"/>
                              <div style="margin-top:8px;color:#ffffff;font-size:18px;
                                font-weight:600;letter-spacing:0.3px;">Fatrans</div>
                              <div style="margin-top:2px;color:#94a3b8;font-size:11px;
                                text-transform:uppercase;letter-spacing:1px;">
                                Asociación de Ahorro y Crédito
                              </div>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:32px 32px 8px;font-size:14px;line-height:1.5;">
                              %s
                              %s
                            </td>
                          </tr>
                          <tr>
                            <td style="background:#f8fafc;padding:20px 32px;
                              border-top:1px solid #e2e8f0;color:#64748b;font-size:11px;
                              line-height:1.5;">
                              <strong style="color:#475569;">Fatrans</strong> · Asociación de
                              Ahorro y Crédito · RIF J-50516835-5<br/>
                              Si no esperabas este correo, podés ignorarlo o escribirnos a
                              <a href="mailto:soporte@fatrans.com.ve"
                                style="color:#16A34A;text-decoration:none;">soporte@fatrans.com.ve</a>
                              <br/><br/>
                              Este mensaje fue enviado automáticamente — no respondas a este buzón.
                            </td>
                          </tr>
                        </table>
                        <p style="margin:14px 0 0;color:#94a3b8;font-size:10px;
                          font-family:Arial,Helvetica,sans-serif;">
                          © 2026 Fatrans — Plataforma digital para socios del sector transporte
                          venezolano.
                        </p>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(escapeHtml(preheader), logoUrl, contenidoHtml, ctaHtml);
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
