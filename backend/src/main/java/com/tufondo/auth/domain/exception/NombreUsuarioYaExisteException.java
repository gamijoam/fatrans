package com.tufondo.auth.domain.exception;

/**
 * Excepción lanzada cuando el nombre de usuario ya existe en el sistema.
 */
public class NombreUsuarioYaExisteException extends RuntimeException {
    
    public NombreUsuarioYaExisteException(String nombreUsuario) {
        super("El nombre de usuario ya existe: " + nombreUsuario);
    }
}
