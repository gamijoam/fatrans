package com.tufondo.auth.application.usecase;

import com.tufondo.auth.application.dto.TokenValidacionDTO;
import com.tufondo.auth.domain.exception.TokenExpiradoException;
import com.tufondo.auth.domain.exception.TokenInvalidoException;
import com.tufondo.auth.domain.model.Usuario;
import com.tufondo.auth.domain.repository.UsuarioRepository;
import com.tufondo.auth.infrastructure.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ValidarTokenUseCase {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public TokenValidacionDTO ejecutar(String token) {
        try {
            if (!jwtService.esAccessTokenValido(token)) {
                return new TokenValidacionDTO(
                        null, null, null, null, null, false
                );
            }

            UUID usuarioId = jwtService.extraerUsuarioId(token);
            String rol = jwtService.extraerRol(token);

            Usuario usuario = usuarioRepository.buscarPorId(usuarioId)
                    .orElse(null);

            if (usuario == null || !usuario.cuentaActiva()) {
                throw new TokenInvalidoException("Usuario inactivo o no encontrado");
            }

            return new TokenValidacionDTO(
                    usuario.id(),
                    usuario.nombreUsuario(),
                    usuario.correoElectronico(),
                    rol,
                    jwtService.extraerExpiracion(token),
                    true
            );

        } catch (ExpiredJwtException e) {
            log.warn("Token expirado: {}", e.getMessage());
            throw new TokenExpiradoException("Token ha expirado");
        } catch (JwtException e) {
            log.warn("Token inválido: {}", e.getMessage());
            throw new TokenInvalidoException("Token inválido");
        }
    }

    public boolean esValido(String token) {
        try {
            return jwtService.esAccessTokenValido(token);
        } catch (Exception e) {
            return false;
        }
    }
}
