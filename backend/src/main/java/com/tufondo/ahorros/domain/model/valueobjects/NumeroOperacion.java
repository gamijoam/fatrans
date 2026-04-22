// com/tufondo/ahorros/domain/model/valueobjects/NumeroOperacion.java
package com.tufondo.ahorros.domain.model.valueobjects;

import java.time.Year;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Value Object para número de operación/movimiento.
 * Formato: MOV-YYYY-XXXXXX (RN-007)
 */
public final class NumeroOperacion {
    private static final String PREFIJO = "MOV";
    private static final int MIN_SECUENCIAL = 1;
    private static final int MAX_SECUENCIAL = 999999;
    
    private final String valor;

    private NumeroOperacion(String valor) {
        this.valor = valor;
    }

    public static NumeroOperacion crear(String valor) {
        if (valor == null || !esFormatoValido(valor)) {
            throw new IllegalArgumentException("Formato inválido de número de operación. Esperado: MOV-YYYY-XXXXXX");
        }
        return new NumeroOperacion(valor);
    }

    public static NumeroOperacion generar() {
        int year = Year.now().getValue();
        int secuencial = ThreadLocalRandom.current().nextInt(MIN_SECUENCIAL, MAX_SECUENCIAL + 1);
        String valor = String.format("%s-%d-%06d", PREFIJO, year, secuencial);
        return new NumeroOperacion(valor);
    }
    
    public static String generarValor() {
        int year = Year.now().getValue();
        int secuencial = ThreadLocalRandom.current().nextInt(MIN_SECUENCIAL, MAX_SECUENCIAL + 1);
        return String.format("%s-%d-%06d", PREFIJO, year, secuencial);
    }

    public String getValor() {
        return valor;
    }

    private static boolean esFormatoValido(String valor) {
        if (valor == null) return false;
        return valor.matches("^MOV-\\d{4}-\\d{6}$");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NumeroOperacion that = (NumeroOperacion) o;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() {
        return valor.hashCode();
    }

    @Override
    public String toString() {
        return valor;
    }
}