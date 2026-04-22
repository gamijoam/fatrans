// 📁 com/tufondo/core/port/UsuarioCreatorPort.java
package com.tufondo.core.port;

import java.util.UUID;

/**
 * Puerto para la creación de usuarios desde otros módulos.
 * Implementado por el módulo AUTH para evitar ciclos de dependencia.
 */
public interface UsuarioCreatorPort {
    
    /**
     * Crea un usuario vinculado a un socio.
     * 
     * @param socioId ID del socio vinculado
     * @param nombreUsuario nombre de usuario
     * @param correo correo electrónico
     * @param passwordTemporal contraseña temporal (será codificada por el adaptador)
     */
    void crearUsuarioVinculado(UUID socioId, String nombreUsuario, String correo, String passwordTemporal);
    
    /**
     * Verifica si existe un nombre de usuario.
     */
    boolean existeNombreUsuario(String nombreUsuario);
}