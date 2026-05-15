package com.tufondo.kyc.domain.model.enums;

/**
 * Estado del flujo biométrico cacheado en {@code verificacion_kyc.estado_biometria}.
 *
 * Se mantiene sincronizado por el use case que registra el resultado del webhook
 * del proveedor (Didit, etc.). Es un cache para evitar JOIN con {@code biometric_verification}
 * en cada listado de la cola del admin.
 *
 * Distinción importante con {@link com.tufondo.kyc.domain.model.enums.EstadoVerificacion}:
 *  - EstadoVerificacion es el estado del KYC global (documental + biométrico).
 *  - EstadoBiometria es solo el sub-resultado biométrico.
 *
 * Una verificación documental puede estar APROBADA mientras la biometría está
 * RECHAZADA — el analista decide si la rechaza globalmente o pide repetir biometría.
 */
public enum EstadoBiometria {

    /** Aún no se inició ningún intento biométrico. Es el estado por defecto. */
    NO_INICIADA,

    /** Sesión biométrica abierta con el proveedor; esperando que el usuario complete. */
    EN_PROGRESO,

    /** Liveness + face match cumplieron los umbrales mínimos. */
    APROBADA,

    /** Liveness o face match fallaron, o el proveedor detectó intento de spoof. */
    RECHAZADA,

    /** Sesión expiró sin que el usuario la completara (timeout del proveedor). */
    EXPIRADA
}
