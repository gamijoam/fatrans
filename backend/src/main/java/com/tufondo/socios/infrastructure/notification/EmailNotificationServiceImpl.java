// 📁 com/tufondo/socios/infrastructure/notification/EmailNotificationServiceImpl.java
package com.tufondo.socios.infrastructure.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementación mock del servicio de email.
 * Por ahora solo loguea los emails enviados.
 */
@Service
@Slf4j
public class EmailNotificationServiceImpl implements EmailNotificationService {
    
    @Override
    public void enviarCredenciales(String email, String nombreUsuario, String passwordTemporal) {
        // NUNCA loguear la contraseña temporal — riesgo LOPDP/seguridad.
        // Solo se registran metadatos suficientes para auditoría operacional.
        log.info("Email de credenciales enviado (mock) a usuario={} (asunto: 'Tu cuenta del Fondo de Ahorro ha sido creada')",
                nombreUsuario);
    }
    
    @Override
    public void enviarNotificacionRechazo(String email, String motivo) {
        log.info("========== EMAIL MOCK: RECHAZO DE SOLICITUD ==========");
        log.info("Para: {}", email);
        log.info("Asunto: Tu solicitud de registro ha sido rechazada");
        log.info("Motivo: {}", motivo);
        log.info("======================================================");
    }
    
    @Override
    public void enviarNotificacionSolicitudRecibida(String email) {
        log.info("========== EMAIL MOCK: SOLICITUD RECIBIDA ==========");
        log.info("Para: {}", email);
        log.info("Asunto: Hemos recibido tu solicitud de registro");
        log.info("Cuerpo: Tu solicitud está siendo procesada. Te notificaremos cuando sea revisada.");
        log.info("====================================================");
    }
}