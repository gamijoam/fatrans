package com.tufondo.socios.application.usecase;

import com.tufondo.auth.domain.exception.CredencialesInvalidasException;
import com.tufondo.auth.domain.exception.UsuarioNoEncontradoException;
import com.tufondo.auth.domain.model.Usuario;
import com.tufondo.auth.domain.repository.UsuarioRepository;
import com.tufondo.auth.infrastructure.service.SecurityAuditService;
import com.tufondo.auth.infrastructure.service.VerificacionService;
import com.tufondo.socios.application.dto.ConfirmarCodigoRequestDTO;
import com.tufondo.socios.application.dto.ConfirmarCodigoResponseDTO;
import com.tufondo.socios.application.dto.EnviarCodigoRequestDTO;
import com.tufondo.socios.application.dto.VerificarPasswordRequestDTO;
import com.tufondo.socios.application.dto.VerificarPasswordResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificacionUseCase {

    private final UsuarioRepository usuarioRepository;
    private final VerificacionService verificacionService;
    private final SecurityAuditService auditService;

    @Transactional
    public VerificarPasswordResponseDTO verificarPassword(UUID usuarioId, VerificarPasswordRequestDTO request,
                                                          HttpServletRequest httpRequest) {
        Usuario usuario = usuarioRepository.buscarPorId(usuarioId)
                .orElseThrow(() -> new CredencialesInvalidasException("Usuario no encontrado"));

        String ip = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        boolean valido = verificacionService.verificarPasswordUsuario(
                usuarioId,
                request.getPassword(),
                usuario.passwordHash(),
                ip,
                userAgent
        );

        if (!valido) {
            throw new CredencialesInvalidasException("Contraseña incorrecta");
        }

        String token = verificacionService.generarTokenVerificacion(usuarioId, ip, userAgent);

        return VerificarPasswordResponseDTO.builder()
                .valido(true)
                .tokenVerificacion(token)
                .build();
    }

    @Transactional
    public String enviarCodigo(UUID usuarioId, EnviarCodigoRequestDTO request, HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        String email = request.getTipo() == com.tufondo.socios.domain.model.enums.TipoVerificacion.EMAIL
                ? request.getValor()
                : null;

        String token = verificacionService.generarYCEnviarCodigo(
                usuarioId,
                request.getTipo(),
                request.getValor(),
                ip,
                userAgent,
                email
        );

        return token;
    }

    @Transactional
    public ConfirmarCodigoResponseDTO confirmarCodigo(UUID usuarioId, ConfirmarCodigoRequestDTO request,
                                                     HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        boolean valido = verificacionService.confirmarCodigo(
                usuarioId,
                request.getToken(),
                request.getCodigo(),
                ip,
                userAgent
        );

        if (!valido) {
            throw new CodigoInvalidoException("Código inválido o expirado");
        }

        return ConfirmarCodigoResponseDTO.builder()
                .valido(true)
                .tokenVerificacion(request.getToken())
                .build();
    }

    public boolean validarToken(UUID usuarioId, String token) {
        return verificacionService.validarTokenVerificacion(usuarioId, token);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    public static class PasswordIncorrectoException extends RuntimeException {
        public PasswordIncorrectoException(String message) {
            super(message);
        }
    }

    public static class CodigoInvalidoException extends RuntimeException {
        public CodigoInvalidoException(String message) {
            super(message);
        }
    }
}