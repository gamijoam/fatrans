package com.tufondo.auth.infrastructure.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Servicio de email para notificaciones de autenticación.
 * 
 * Implementación MOCK que solo loguea los emails.
 * En producción, este servicio se implementaría con un proveedor real (SendGrid, AWS SES, etc.)
 */
@Slf4j
@Service
public class EmailService {

    /**
     * Envía un email de recuperación de contraseña.
     *
     * @param destinatario Email del destinatario
     * @param nombreUsuario Nombre de usuario
     * @param resetLink Link de recuperación
     */
    public void enviarEmailRecuperacionPassword(String destinatario, String nombreUsuario, String resetLink) {
        log.info("========= EMAIL MOCK: RECUPERACIÓN DE CONTRASEÑA =========");
        log.info("Para: {}", destinatario);
        log.info("Usuario: {}", nombreUsuario);
        log.info("Link de recuperación: {}", resetLink);
        log.info("Este es un email mock - En producción se enviaría un email real");
        log.info("============================================================");
    }

    /**
     * Envía un email confirmando el cambio de contraseña.
     *
     * @param destinatario Email del destinatario
     * @param nombreUsuario Nombre de usuario
     */
    public void enviarEmailPasswordCambiada(String destinatario, String nombreUsuario) {
        log.info("========= EMAIL MOCK: CONTRASEÑA CAMBIADA =========");
        log.info("Para: {}", destinatario);
        log.info("Usuario: {}", nombreUsuario);
        log.info("Mensaje: Su contraseña ha sido cambiada exitosamente");
        log.info("Este es un email mock - En producción se enviaría un email real");
        log.info("====================================================");
    }

    /**
     * Envía un email de bienvenida con credenciales temporales.
     *
     * @param destinatario Email del destinatario
     * @param nombreUsuario Nombre de usuario
     * @param passwordTemporal Contraseña temporal
     */
    public void enviarEmailBienvenida(String destinatario, String nombreUsuario, String passwordTemporal) {
        log.info("========= EMAIL MOCK: BIENVENIDA =========");
        log.info("Para: {}", destinatario);
        log.info("Usuario: {}", nombreUsuario);
        log.info("Password temporal: {}", passwordTemporal);
        log.info("Este es un email mock - En producción se enviaría un email real");
        log.info("==========================================");
    }
}
