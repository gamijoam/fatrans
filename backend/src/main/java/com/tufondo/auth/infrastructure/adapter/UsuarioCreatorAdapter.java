// 📁 com/tufondo/auth/infrastructure/adapter/UsuarioCreatorAdapter.java
package com.tufondo.auth.infrastructure.adapter;

import com.tufondo.auth.domain.model.Usuario;
import com.tufondo.auth.domain.model.enums.Rol;
import com.tufondo.auth.domain.repository.UsuarioRepository;
import com.tufondo.core.port.UsuarioCreatorPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Adaptador que implementa el puerto UsuarioCreatorPort.
 * Permite que el módulo SOCIOS cree usuarios sin depender directamente de AUTH.
 */
@Component
@RequiredArgsConstructor
public class UsuarioCreatorAdapter implements UsuarioCreatorPort {
    
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void crearUsuarioVinculado(UUID socioId, String nombreUsuario, String correo, String passwordTemporal) {
        // Verificar si el usuario ya existe
        if (usuarioRepository.existePorNombreUsuario(nombreUsuario)) {
            throw new IllegalStateException("El nombre de usuario ya existe: " + nombreUsuario);
        }
        
        // Crear el usuario
        Usuario nuevoUsuario = Usuario.crear(
                nombreUsuario,
                correo,
                passwordEncoder.encode(passwordTemporal),
                "Socio", // nombreCompleto
                Rol.SOCIO,
                socioId
        );
        
        usuarioRepository.guardar(nuevoUsuario);
    }
    
    @Override
    public boolean existeNombreUsuario(String nombreUsuario) {
        return usuarioRepository.existePorNombreUsuario(nombreUsuario);
    }
}