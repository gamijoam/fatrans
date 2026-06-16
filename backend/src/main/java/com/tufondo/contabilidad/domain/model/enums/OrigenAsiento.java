package com.tufondo.contabilidad.domain.model.enums;

/**
 * Origen del asiento contable — qué evento de negocio lo disparó.
 *
 * <p>Permite filtrar reportes ("todos los depósitos del mes", "todos los
 * cierres mensuales") y trazar cada asiento al módulo que lo generó.</p>
 *
 * <p>Los valores deben coincidir EXACTAMENTE con el CHECK constraint de
 * {@code asientos_contables.origen} en V22. Cambios en este enum requieren
 * migration que actualice el CHECK.</p>
 */
public enum OrigenAsiento {
    /** Depósito de un socio en su cuenta de ahorro. */
    AHORRO_DEPOSITO,
    /** Retiro de un socio de su cuenta de ahorro. */
    AHORRO_RETIRO,
    /** Acreditación de intereses sobre cuentas de ahorro. */
    AHORRO_INTERES,

    /** Desembolso inicial de un crédito al socio. */
    CREDITO_DESEMBOLSO,
    /** Cobro de una cuota de crédito (capital + intereses). */
    CREDITO_COBRO,
    /** Devengo de intereses sobre cartera de créditos. */
    CREDITO_INTERES,

    /** Asiento manual ingresado por un contador (ej. ajustes). */
    MANUAL,
    /** Asiento de cierre mensual o anual. */
    CIERRE,
    /** Asiento de reversión de otro asiento previo. */
    REVERSION,
    /** Ajuste contable (corrección de error, reclasificación). */
    AJUSTE,
}
