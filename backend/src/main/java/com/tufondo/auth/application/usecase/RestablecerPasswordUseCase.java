package com.tufondo.auth.application.usecase;

import com.tufondo.auth.application.dto.MensajeResponseDTO;
import com.tufondo.auth.application.dto.ResetPasswordRequestDTO;
import com.tufondo.auth.domain.exception.PasswordNoCumpleRequisitosException;
import com.tufondo.auth.domain.exception.TokenRecuperacionInvalidoException;
import com.tufondo.auth.domain.model.PasswordResetToken;
import com.tufondo.auth.domain.model.Usuario;
import com.tufondo.auth.domain.repository.PasswordResetTokenRepository;
import com.tufondo.auth.domain.repository.UsuarioRepository;
import com.tufondo.auth.infrastructure.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Caso de uso para restablecer la contraseña usando un token de recuperación.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RestablecerPasswordUseCase {

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // Patrón para validar requisitos de password:
    // - Mínimo 8 caracteres
    // - Al menos 1 mayúscula
    // - Al menos 1 número
    // - Al menos 1 carácter especial
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );

    /**
     * Restablece la contraseña de un usuario usando un token válido.
     *
     * @param request DTO con el token y la nueva contraseña
     * @return Mensaje de éxito
     * @throws TokenRecuperacionInvalidoException si el token es inválido o expirado
     * @throws PasswordNoCumpleRequisitosException si la password no cumple los requisitos
     */
    @Transactional
    public MensajeResponseDTO ejecutar(ResetPasswordRequestDTO request) {
        log.info("Solicitud de restablecimiento de password con token");

        // 1. Validar formato de password
        if (!esPasswordValida(request.nuevaPassword())) {
            log.warn("Password no cumple requisitos de seguridad");
            throw new PasswordNoCumpleRequisitosException(
                    "La contraseña debe tener al menos 8 caracteres, una mayúscula, un número y un carácter especial"
            );
        }

        // 2. Buscar token válido
        Optional<PasswordResetToken> tokenOpt = tokenRepository.buscarTokenValido(request.token());
        if (tokenOpt.isEmpty()) {
            log.warn("Token de recuperación inválido o expirado");
            throw new TokenRecuperacionInvalidoException("Token inválido o expirado");
        }

        PasswordResetToken token = tokenOpt.get();

        // 3. Verificar que el token es válido
        if (!token.esValido()) {
            log.warn("Token de recuperación no es válido: usado={}, expirado={}", 
                    token.used(), token.estaExpirado());
            throw new TokenRecuperacionInvalidoException("Token inválido o expirado");
        }

        // 4. Buscar el usuario
        Optional<Usuario> usuarioOpt = usuarioRepository.buscarPorId(token.usuarioId());
        if (usuarioOpt.isEmpty()) {
            log.error("Usuario no encontrado para token: {}", token.id());
            throw new TokenRecuperacionInvalidoException("Token inválido o expirado");
        }

        Usuario usuario = usuarioOpt.get();

        // 5. Codificar nueva password y actualizar usuario
        String nuevoPasswordHash = passwordEncoder.encode(request.nuevaPassword());
        Usuario usuarioActualizado = Usuario.desdeParametros(
                usuario.id(),
                usuario.nombreUsuario(),
                usuario.correoElectronico(),
                nuevoPasswordHash,
                usuario.nombreCompleto(),
                usuario.rol(),
                usuario.socioId(),
                usuario.cuentaActiva(),
                usuario.fechaCreacion(),
                java.time.Instant.now(),
                0, // Reset intentos fallidos
                null // Reset fecha bloqueo
        );

        usuarioRepository.actualizar(usuarioActualizado);

        // 6. Marcar token como usado
        tokenRepository.marcarComoUsado(token.token());

        // 7. Enviar email de confirmación (mock por ahora)
        emailService.enviarEmailPasswordCambiada(
                usuario.correoElectronico(),
                usuario.nombreUsuario()
        );

        log.info("Password restablecida exitosamente para usuario: {}", usuario.id());

        return new MensajeResponseDTO("Contraseña actualizada exitosamente");
    }

    /**
     * Valida que la password cumple con los requisitos de seguridad.
     */
    private boolean esPasswordValida(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }
}
