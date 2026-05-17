package com.tufondo.notificaciones.domain.model.enums;

/**
 * Tipos de notificación in-app del sistema (issue #214).
 *
 * <p>Cada valor representa un evento de dominio que dispara una notificación
 * al usuario destinatario. Los disparadores reales se conectan en el PR-C;
 * este PR (PR-A) solo define el catálogo y los endpoints.</p>
 *
 * <p>El nombre del valor debe coincidir EXACTAMENTE con el CHECK constraint
 * de la migración V18 — agregar un valor aquí requiere también una
 * migración Flyway que actualice el constraint (lección aprendida del
 * incidente del rol fantasma, hotfix #238).</p>
 */
public enum TipoNotificacion {
    // === Notificaciones para el SOCIO ===
    KYC_APROBADO,
    KYC_RECHAZADO,
    KYC_REQUIERE_INFO,

    CREDITO_APROBADO,
    CREDITO_RECHAZADO,
    CREDITO_DESEMBOLSADO,

    CUOTA_PROXIMA_VENCER,
    CUOTA_VENCIDA,

    DEPOSITO_RECIBIDO,
    RETIRO_PROCESADO,

    NUEVO_DISPOSITIVO_LOGIN,

    // === Notificaciones para el ADMIN ===
    ADMIN_NUEVA_SOLICITUD,
    ADMIN_NUEVO_KYC,

    // === Genérica / fallback ===
    GENERAL
}
