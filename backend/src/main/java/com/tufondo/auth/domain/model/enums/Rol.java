package com.tufondo.auth.domain.model.enums;

public enum Rol {
    SOCIO,
    ADMIN,
    SUPER_ADMIN,
    CAJERO,
    ANALISTA_KYC,
    /**
     * Issue #207: rol dedicado a evaluar y aprobar (NO desembolsar) créditos.
     * La jerarquía en {@code SecurityConfig} ya lo declaraba bajo ADMIN
     * pero no existía aquí — era un "rol fantasma" que nunca podía emitirse
     * en el JWT.
     *
     * <p>Separation of Duties: este rol puede evaluar y aprobar/rechazar,
     * pero NO desembolsar (esa acción queda solo en ADMIN/SISTEMA para
     * mantener separación entre quien autoriza y quien ejecuta el movimiento
     * de fondos).</p>
     */
    ANALISTA_CREDITO,
    SISTEMA
}
