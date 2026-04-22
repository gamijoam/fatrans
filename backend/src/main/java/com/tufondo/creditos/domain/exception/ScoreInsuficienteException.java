// com/tufondo/creditos/domain/exception/ScoreInsuficienteException.java
package com.tufondo.creditos.domain.exception;

/**
 * Excepción cuando el score crediticio es insuficiente para aprobación.
 */
public class ScoreInsuficienteException extends RuntimeException {
    
    public ScoreInsuficienteException(Integer score, Integer minimo) {
        super(String.format("Score insuficiente: %d (mínimo requerido: %d)", score, minimo));
    }
}
