package com.tufondo.auth.application.usecase;

import com.tufondo.auth.application.dto.LoginResponseDTO;
import com.tufondo.auth.domain.exception.TokenInvalidoException;
import com.tufondo.auth.domain.exception.UsuarioNoEncontradoException;
import com.tufondo.auth.domain.model.Usuario;
import com.tufondo.auth.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ObtenerUsuarioActualUseCase {

    private final UsuarioRepository usuarioRepository;

    public LoginResponseDTO.UsuarioDTO ejecutar() {
        String usuarioId = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        if (usuarioId == null || usuarioId.isBlank()) {
            throw new TokenInvalidoException("No hay usuario autenticado");
        }

        Usuario usuario = usuarioRepository.buscarPorId(java.util.UUID.fromString(usuarioId))
                .orElseThrow(() -> new UsuarioNoEncontradoException(
                        "Usuario no encontrado: " + usuarioId));

        return new LoginResponseDTO.UsuarioDTO(
                usuario.id().toString(),
                usuario.nombreUsuario(),
                usuario.correoElectronico(),
                usuario.nombreCompleto(),
                usuario.rol().name(),
                usuario.debeCambiarPassword()
        );
    }
}
