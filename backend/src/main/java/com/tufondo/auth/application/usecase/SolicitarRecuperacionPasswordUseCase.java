package com.tufondo.auth.application.usecase;

import com.tufondo.auth.application.dto.MensajeResponseDTO;
import com.tufondo.auth.domain.model.PasswordResetToken;
import com.tufondo.auth.domain.model.Usuario;
import com.tufondo.auth.domain.repository.PasswordResetTokenRepository;
import com.tufondo.auth.domain.repository.UsuarioRepository;
import com.tufondo.auth.infrastructure.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Caso de uso para solicitar recuperación de contraseña.
 * 
 * Por seguridad, siempre retorna éxito regardless de si el email/usuario existe.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SolicitarRecuperacionPasswordUseCase {

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    /**
     * Solicita recuperación de contraseña para un usuario.
     * 
     * Por seguridad, SIEMPRE retorna el mismo mensaje de éxito
     * para no revelar si el email existe o no.
     *
     * @param identificador Email o nombre de usuario
     * @return Mensaje genérico de confirmación
     */
    @Transactional
    public MensajeResponseDTO ejecutar(String identificador) {
        log.info("Solicitud de recuperación de password para identificador: {}", identificador);

        // Siempre retornamos éxito por seguridad (no revelar si el email existe)
        
        // 1. Intentar buscar por nombre de usuario
        Optional<Usuario> usuarioOpt = usuarioRepository.buscarPorNombreUsuario(identificador);
        
        // 2. Si no se encontró, intentar por email
        if (usuarioOpt.isEmpty()) {
            usuarioOpt = usuarioRepository.buscarPorCorreoElectronico(identificador);
        }

        // Si encontramos el usuario, procesamos la solicitud
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            log.info("Usuario encontrado para recuperación: {}", usuario.id());

            // Invalidar tokens anteriores del usuario
            tokenRepository.eliminarTokensPorUsuario(usuario.id());

            // Crear nuevo token
            PasswordResetToken token = PasswordResetToken.crear(usuario.id());
            tokenRepository.guardar(token);

            // Enviar email con el token (mock por ahora)
            String resetLink = "/auth/reset-password?token=" + token.token();
            emailService.enviarEmailRecuperacionPassword(
                    usuario.correoElectronico(),
                    usuario.nombreUsuario(),
                    resetLink
            );

            log.info("Token de recuperación generado para usuario: {}", usuario.id());
        } else {
            // Usuario no encontrado - por seguridad, solo logueamos y continuamos
            log.info("No se encontró usuario para identificador: {} (esto es esperado si el usuario no existe)", 
                    identificador);
        }

        // Siempre retornamos el mismo mensaje de éxito
        return new MensajeResponseDTO("Si el email existe, se ha enviado un enlace de recuperación");
    }
}
