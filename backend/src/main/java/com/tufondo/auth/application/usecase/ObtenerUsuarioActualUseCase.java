package com.tufondo.auth.application.usecase;

import com.tufondo.auth.application.dto.LoginResponseDTO;
import com.tufondo.auth.domain.exception.TokenInvalidoException;
import com.tufondo.auth.domain.exception.UsuarioNoEncontradoException;
import com.tufondo.auth.domain.model.Usuario;
import com.tufondo.auth.domain.repository.UsuarioRepository;
import com.tufondo.auth.infrastructure.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ObtenerUsuarioActualUseCase {

    private final UsuarioRepository usuarioRepository;

    public LoginResponseDTO.UsuarioDTO ejecutar() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new TokenInvalidoException("No hay usuario autenticado");
        }

        Object principal = authentication.getPrincipal();
        UUID socioId = null;

        if (principal instanceof AuthenticatedUser authUser) {
            socioId = authUser.getSocioId();
        }

        String usuarioId = authentication.getName();

        if (usuarioId == null || usuarioId.isBlank() || "anonymousUser".equals(usuarioId)) {
            throw new TokenInvalidoException("No hay usuario autenticado válido");
        }

        Usuario usuario = usuarioRepository.buscarPorId(UUID.fromString(usuarioId))
                .orElseThrow(() -> new UsuarioNoEncontradoException(
                        "Usuario no encontrado: " + usuarioId));

        return new LoginResponseDTO.UsuarioDTO(
                usuario.id().toString(),
                usuario.nombreUsuario(),
                usuario.correoElectronico(),
                usuario.nombreCompleto(),
                usuario.rol().name(),
                socioId != null ? socioId.toString() : (usuario.socioId() != null ? usuario.socioId().toString() : null),
                usuario.debeCambiarPassword()
        );
    }
}
