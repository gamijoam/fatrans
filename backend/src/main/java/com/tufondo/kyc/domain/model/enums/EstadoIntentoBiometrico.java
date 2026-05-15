package com.tufondo.kyc.domain.model.enums;

/**
 * Estado individual de un intento biométrico (registro en {@code biometric_verification}).
 * Cada intento es atómico: una sesión con un proveedor, con su resultado.
 *
 * Un socio puede tener varios intentos (reintenta tras fallar liveness, por ejemplo) —
 * por eso el modelo separa este enum de {@link EstadoBiometria}, que es el cache
 * agregado en {@code verificacion_kyc}.
 */
public enum EstadoIntentoBiometrico {

    /** Sesión creada en el proveedor pero usuario aún no abrió el widget. */
    PENDIENTE,

    /** Usuario abrió el widget; el proveedor está procesando. */
    EN_PROGRESO,

    APROBADA,
    RECHAZADA,

    /** El proveedor cerró la sesión por timeout sin que el usuario terminara. */
    EXPIRADA,

    /** El usuario o un admin canceló el intento explícitamente. */
    CANCELADA
}
