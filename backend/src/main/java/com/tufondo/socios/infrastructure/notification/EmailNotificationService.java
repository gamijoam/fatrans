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

    /**
     * Notifica al socio que su verificación KYC fue aprobada.
     * Best-effort: si falla SMTP no propaga error al caller (mismo contract
     * que los demás métodos de este interface).
     */
    void enviarKycAprobado(String email, String nombreCompleto);

    /**
     * Notifica al socio que su verificación KYC fue rechazada.
     *
     * @param motivo motivo del rechazo (lo escribió el analista). Puede ser null/blank;
     *               en ese caso se muestra un texto genérico.
     */
    void enviarKycRechazado(String email, String nombreCompleto, String motivo);

    /**
     * Notifica al socio que el analista necesita más información para
     * completar la verificación.
     *
     * @param detalle qué información se necesita (lo escribió el analista). Puede ser null/blank.
     */
    void enviarKycRequiereInfo(String email, String nombreCompleto, String detalle);
}