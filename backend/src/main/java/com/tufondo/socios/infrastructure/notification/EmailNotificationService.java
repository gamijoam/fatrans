// 📁 com/tufondo/socios/infrastructure/notification/EmailNotificationService.java
package com.tufondo.socios.infrastructure.notification;

/**
 * Puerto para el servicio de notificaciones por email.
 */
public interface EmailNotificationService {
    
    /**
     * Envía credenciales de acceso al usuario.
     */
    void enviarCredenciales(String email, String nombreUsuario, String passwordTemporal);
    
    /**
     * Envía notificación de rechazo de solicitud.
     */
    void enviarNotificacionRechazo(String email, String motivo);
    
    /**
     * Envía confirmación de solicitud recibida.
     */
    void enviarNotificacionSolicitudRecibida(String email);
}