package com.tufondo.contabilidad.domain.model.enums;

/**
 * Clasificación primaria de una cuenta del plan contable según VEN-NIF
 * (Norma Venezolana de Información Financiera, basada en NIIF/IFRS).
 *
 * <p>Cada tipo tiene una naturaleza de saldo "natural" — la que aplica cuando
 * la cuenta opera en condiciones normales:</p>
 *
 * <ul>
 *   <li>{@link #ACTIVO} y {@link #EGRESO} → naturaleza {@code DEUDORA}
 *       (aumentan en el DEBE, disminuyen en el HABER)</li>
 *   <li>{@link #PASIVO}, {@link #PATRIMONIO} y {@link #INGRESO} → naturaleza
 *       {@code ACREEDORA} (aumentan en el HABER, disminuyen en el DEBE)</li>
 *   <li>{@link #CUENTA_ORDEN} → naturaleza configurable (deudoras de orden
 *       vs acreedoras de orden, según el caso)</li>
 * </ul>
 *
 * <p>Estos tipos se mapean al primer dígito del código contable (1=ACTIVO,
 * 2=PASIVO, 3=PATRIMONIO, 4=INGRESO, 5=EGRESO, 6=CUENTA_ORDEN), que es la
 * convención usada en planes de cuentas de cooperativas y cajas de ahorro
 * venezolanas (SUDECA/SUNACOOP).</p>
 */
public enum TipoCuentaContable {
    /** Bienes y derechos de la entidad. Código inicial: 1. */
    ACTIVO,
    /** Obligaciones con terceros. Código inicial: 2. */
    PASIVO,
    /** Recursos propios de la entidad (aportes, reservas, resultados). Código inicial: 3. */
    PATRIMONIO,
    /** Ingresos del ejercicio. Código inicial: 4. */
    INGRESO,
    /** Egresos / gastos del ejercicio. Código inicial: 5. */
    EGRESO,
    /**
     * Cuentas de orden (garantías recibidas/otorgadas, contingencias).
     * No afectan el balance pero deben reportarse. Código inicial: 6.
     */
    CUENTA_ORDEN;

    /**
     * Devuelve la naturaleza de saldo "natural" para este tipo. Esta es la
     * que se asigna por default al crear la cuenta, pero puede sobreescribirse
     * para casos específicos (ej. una cuenta de orden acreedora).
     */
    public NaturalezaSaldo naturalezaNatural() {
        return switch (this) {
            case ACTIVO, EGRESO -> NaturalezaSaldo.DEUDORA;
            case PASIVO, PATRIMONIO, INGRESO -> NaturalezaSaldo.ACREEDORA;
            // Para cuentas de orden el "natural" es DEUDORA pero realmente
            // depende del subgrupo (deudoras vs acreedoras de orden). El seed
            // del plan lo asigna explícitamente.
            case CUENTA_ORDEN -> NaturalezaSaldo.DEUDORA;
        };
    }
}
