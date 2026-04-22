// com/tufondo/creditos/domain/exception/CreditoActivoExistenteException.java
package com.tufondo.creditos.domain.exception;

import java.util.UUID;

/**
 * Excepción cuando un socio ya tiene un crédito activo.
 */
public class CreditoActivoExistenteException extends RuntimeException {

    public CreditoActivoExistenteException(UUID socioId) {
        super(String.format("El socio %s ya tiene un crédito activo en estado DESEMBOLSADO", socioId));
    }
}
