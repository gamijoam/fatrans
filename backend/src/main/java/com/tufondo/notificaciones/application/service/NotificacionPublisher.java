package com.tufondo.notificaciones.application.service;

import com.tufondo.auth.domain.model.Usuario;
import com.tufondo.auth.domain.repository.UsuarioRepository;
import com.tufondo.notificaciones.domain.model.Notificacion;
import com.tufondo.notificaciones.domain.model.enums.PrioridadNotificacion;
import com.tufondo.notificaciones.domain.model.enums.TipoNotificacion;
import com.tufondo.notificaciones.domain.repository.NotificacionRepository;
import com.tufondo.socios.infrastructure.notification.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio "publicador" de notificaciones — fachada con métodos semánticos
 * del dominio (issue #214 PR-C).
 *
 * <p>Los use cases de KYC, créditos, depósitos, etc. inyectan este servicio
 * y llaman métodos como {@code notificarSocioKycAprobado(socioId)} sin
 * preocuparse por la resolución socioId→usuarioId, ni por construir el
 * mensaje, ni por elegir la prioridad. Eso queda centralizado aquí.</p>
 *
 * <p><strong>Resiliencia</strong>: si la inserción de la notificación falla
 * (BD caída, contraint violation, etc.), el caller NO debe abortar su
 * operación principal. Por eso usamos {@code REQUIRES_NEW} y atrapamos
 * Throwable — un fallo de notificación nunca debe romper aprobar un KYC
 * o desembolsar un crédito.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionPublisher {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;
    /**
     * Servicio de envío de email. Inyectado para que las notificaciones de
     * KYC (y otras de alto valor para el socio) también lleguen por correo
     * además de la notificación in-app. El servicio es best-effort: si SMTP
     * falla, el flujo del caller NO se ve afectado.
     */
    private final EmailNotificationService emailService;

    // === Notificaciones a SOCIO (resuelven socioId → usuarioId) ===

    public void notificarSocioKycAprobado(UUID socioId) {
        publicarASocio(socioId, Notificacion.builder()
                .tipo(TipoNotificacion.KYC_APROBADO)
                .titulo("Tu verificación KYC fue aprobada")
                .mensaje("¡Felicidades! Ya puedes acceder a todas las funciones del fondo: solicitar créditos, depósitos y retiros sin límites adicionales.")
                .linkAccion("/dashboard/kyc")
                .prioridad(PrioridadNotificacion.NORMAL)
                .build());
        // Email además de notificación in-app: el KYC aprobado es un evento de
        // alto valor que el socio espera por mail (caso real reportado por el
        // admin de QA, 19-may-2026 — el socio no se enteraba de la aprobación
        // porque solo se guardaba la notificación interna).
        enviarEmailASocio(socioId, (email, nombre) -> emailService.enviarKycAprobado(email, nombre),
                "KYC_APROBADO");
    }

    public void notificarSocioKycRechazado(UUID socioId, String motivo) {
        String texto = motivo != null && !motivo.isBlank()
                ? "Motivo: " + motivo + ". Revisa los documentos y vuelve a enviar."
                : "Revisa el detalle, corrige los documentos y vuelve a enviar.";
        publicarASocio(socioId, Notificacion.builder()
                .tipo(TipoNotificacion.KYC_RECHAZADO)
                .titulo("Tu verificación KYC fue rechazada")
                .mensaje(texto)
                .linkAccion("/dashboard/kyc")
                .prioridad(PrioridadNotificacion.URGENTE)
                .build());
        enviarEmailASocio(socioId, (email, nombre) -> emailService.enviarKycRechazado(email, nombre, motivo),
                "KYC_RECHAZADO");
    }

    public void notificarSocioKycRequiereInfo(UUID socioId, String detalle) {
        String texto = detalle != null && !detalle.isBlank()
                ? "El analista necesita: " + detalle
                : "El analista necesita información adicional para continuar.";
        publicarASocio(socioId, Notificacion.builder()
                .tipo(TipoNotificacion.KYC_REQUIERE_INFO)
                .titulo("Tu KYC requiere información adicional")
                .mensaje(texto)
                .linkAccion("/dashboard/kyc")
                .prioridad(PrioridadNotificacion.NORMAL)
                .build());
        enviarEmailASocio(socioId, (email, nombre) -> emailService.enviarKycRequiereInfo(email, nombre, detalle),
                "KYC_REQUIERE_INFO");
    }

    public void notificarSocioCreditoAprobado(UUID socioId, BigDecimal monto, String moneda, String numeroSolicitud) {
        publicarASocio(socioId, Notificacion.builder()
                .tipo(TipoNotificacion.CREDITO_APROBADO)
                .titulo("Tu crédito fue aprobado")
                .mensaje(String.format("Solicitud %s aprobada por %s %s. Pronto recibirás el desembolso.",
                        safe(numeroSolicitud), safe(moneda), monto != null ? monto.toPlainString() : "—"))
                .linkAccion("/dashboard/creditos")
                .prioridad(PrioridadNotificacion.NORMAL)
                .build());
    }

    public void notificarSocioCreditoRechazado(UUID socioId, String numeroSolicitud, String motivo) {
        String texto = motivo != null && !motivo.isBlank()
                ? "Motivo: " + motivo
                : "Revisa el detalle desde tu panel de créditos.";
        publicarASocio(socioId, Notificacion.builder()
                .tipo(TipoNotificacion.CREDITO_RECHAZADO)
                .titulo("Tu solicitud de crédito fue rechazada")
                .mensaje(String.format("Solicitud %s. %s", safe(numeroSolicitud), texto))
                .linkAccion("/dashboard/creditos")
                .prioridad(PrioridadNotificacion.NORMAL)
                .build());
    }

    public void notificarSocioCreditoDesembolsado(UUID socioId, BigDecimal monto, String moneda, String numeroSolicitud) {
        publicarASocio(socioId, Notificacion.builder()
                .tipo(TipoNotificacion.CREDITO_DESEMBOLSADO)
                .titulo("Tu crédito fue desembolsado")
                .mensaje(String.format("Recibiste %s %s en tu cuenta (solicitud %s). Revisa el plan de amortización.",
                        safe(moneda), monto != null ? monto.toPlainString() : "—", safe(numeroSolicitud)))
                .linkAccion("/dashboard/creditos")
                .prioridad(PrioridadNotificacion.NORMAL)
                .build());
    }

    // === Helper interno: resolver socioId → usuarioId y persistir ===

    /**
     * Resuelve el {@code socioId} al {@code usuarioId} (destinatario real) y
     * persiste la notificación en una transacción separada con manejo de
     * errores defensivo: una falla aquí NUNCA propaga al caller.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publicarASocio(UUID socioId, Notificacion plantilla) {
        try {
            if (socioId == null) {
                log.warn("notificación skip: socioId es null (tipo={})", plantilla.getTipo());
                return;
            }
            Optional<UUID> usuarioId = usuarioRepository.buscarPorSocioId(socioId)
                    .map(u -> u.id());
            if (usuarioId.isEmpty()) {
                // Sin usuario asociado al socio (caso raro pero posible en
                // datos legacy): logueamos y skipeamos en vez de romper el
                // flujo del caller.
                log.warn("notificación skip: socio {} no tiene usuario asociado (tipo={})",
                        socioId, plantilla.getTipo());
                return;
            }
            persistir(usuarioId.get(), plantilla);
        } catch (Throwable t) {
            // Defensivo extra: ninguna excepción debe propagar.
            log.error("Falló al publicar notificación tipo={} a socioId={}: {}",
                    plantilla.getTipo(), socioId, t.getMessage(), t);
        }
    }

    /**
     * Variante para notificar directamente al usuarioId (admin, sistema)
     * sin pasar por la resolución socio → usuario.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publicarAUsuario(UUID usuarioId, Notificacion plantilla) {
        try {
            if (usuarioId == null) {
                log.warn("notificación skip: usuarioId es null (tipo={})", plantilla.getTipo());
                return;
            }
            persistir(usuarioId, plantilla);
        } catch (Throwable t) {
            log.error("Falló al publicar notificación tipo={} a usuarioId={}: {}",
                    plantilla.getTipo(), usuarioId, t.getMessage(), t);
        }
    }

    private void persistir(UUID destinatarioId, Notificacion plantilla) {
        Notificacion completa = Notificacion.builder()
                .id(UUID.randomUUID())
                .destinatarioId(destinatarioId)
                .tipo(plantilla.getTipo())
                .titulo(plantilla.getTitulo())
                .mensaje(plantilla.getMensaje())
                .linkAccion(plantilla.getLinkAccion())
                .prioridad(plantilla.getPrioridad() != null
                        ? plantilla.getPrioridad()
                        : PrioridadNotificacion.NORMAL)
                .metadata(plantilla.getMetadata())
                .leida(false)
                .createdAt(Instant.now())
                .build();
        notificacionRepository.guardar(completa);
        log.debug("Notificación creada: destinatario={}, tipo={}", destinatarioId, plantilla.getTipo());
    }

    private static String safe(String s) {
        return s != null ? s : "";
    }

    // === Email helpers ===

    /**
     * Functional interface usada por {@link #enviarEmailASocio} para parametrizar
     * el método específico del EmailNotificationService que queremos invocar
     * (aprobado / rechazado / requiere info) sin duplicar la resolución
     * socioId → (email, nombreCompleto).
     */
    @FunctionalInterface
    private interface EmailSender {
        void send(String email, String nombreCompleto);
    }

    /**
     * Resuelve socioId → (email del usuario, nombreCompleto) y delega al
     * {@code emailSender}. Best-effort: cualquier excepción se loguea y se
     * traga — nunca debe romper el flujo del use case que llamó al publisher.
     *
     * <p>Notar que esto NO está en una @Transactional propia: solo lee del
     * UsuarioRepository (idempotente) y llama al SMTP (efecto externo). El
     * mail no es transaccional con la BD — si el COMMIT del KYC falla, el
     * mail YA pudo haberse mandado (al revés también puede pasar). Para el
     * caso de KYC eso es aceptable: el peor escenario es un email enviado
     * sin cambio persistente, que el socio interpreta como un error puntual
     * y vuelve a verificar en la app.</p>
     */
    private void enviarEmailASocio(UUID socioId, EmailSender emailSender, String tipoTag) {
        try {
            if (socioId == null) {
                log.debug("Skip email {}: socioId null", tipoTag);
                return;
            }
            Optional<Usuario> usuario = usuarioRepository.buscarPorSocioId(socioId);
            if (usuario.isEmpty()) {
                log.warn("Skip email {}: no hay usuario para socioId={}", tipoTag, socioId);
                return;
            }
            String email = usuario.get().correoElectronico();
            String nombre = usuario.get().nombreCompleto();
            if (email == null || email.isBlank()) {
                log.warn("Skip email {}: usuario {} sin correo", tipoTag, usuario.get().id());
                return;
            }
            emailSender.send(email, nombre);
        } catch (Throwable t) {
            // Defensivo: cualquier excepción del SMTP o resolución debe quedar
            // contenida acá — el flujo del use case que disparó la notificación
            // sigue su curso.
            log.error("Falló envío de email {} a socioId={}: {}", tipoTag, socioId, t.getMessage(), t);
        }
    }
}
