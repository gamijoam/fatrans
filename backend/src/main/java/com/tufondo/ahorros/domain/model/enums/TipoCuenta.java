// com/tufondo/ahorros/domain/model/enums/TipoCuenta.java
package com.tufondo.ahorros.domain.model.enums;

/**
 * Tipos de cuenta de ahorro disponibles.
 * RN-001: Un socio solo puede tener una cuenta por tipo.
 */
public enum TipoCuenta {
    AHORRO,      // Cuenta estándar
    NOMINA,      // Cuenta de nómina del socio
    PLAZO_FIJO   // Depósito a plazo fijo
}