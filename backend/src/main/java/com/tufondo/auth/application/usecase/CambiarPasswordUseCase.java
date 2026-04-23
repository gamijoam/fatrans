package com.tufondo.auth.application.usecase;

import com.tufondo.auth.application.dto.CambiarPasswordRequestDTO;
import com.tufondo.auth.application.dto.MensajeResponseDTO;
import com.tufondo.auth.domain.exception.CredencialesInvalidasException;
import com.tufondo.auth.domain.exception.PasswordNoCumpleRequisitosException;
import com.tufondo.auth.domain.exception.PasswordReutilizadaException;
import com.tufondo.auth.domain.exception.UsuarioNoEncontradoException;
import com.tufondo.auth.domain.model.Usuario;
import com.tufondo.auth.domain.repository.PasswordHistoryRepository;
import com.tufondo.auth.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class CambiarPasswordUseCase {

    private final UsuarioRepository usuarioRepository;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );

    @Transactional
    public MensajeResponseDTO ejecutar(CambiarPasswordRequestDTO request) {
        String usuarioId = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        if (usuarioId == null || usuarioId.isBlank()) {
            throw new CredencialesInvalidasException("No hay usuario autenticado");
        }

        UUID userUuid = UUID.fromString(usuarioId);

        Usuario usuario = usuarioRepository.buscarPorId(userUuid)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.passwordActual(), usuario.passwordHash())) {
            log.warn("Intento de cambio de password con password actual incorrecta para usuario: {}", usuarioId);
            throw new CredencialesInvalidasException("La contraseña actual es incorrecta");
        }

        if (!esPasswordValida(request.nuevoPassword())) {
            log.warn("Password nueva no cumple requisitos para usuario: {}", usuarioId);
            throw new PasswordNoCumpleRequisitosException(
                    "La nueva contraseña debe tener al menos 8 caracteres, una mayúscula, un número y un carácter especial"
            );
        }

        String nuevoPasswordHash = passwordEncoder.encode(request.nuevoPassword());

        if (passwordHistoryRepository.existePasswordReutilizada(userUuid, nuevoPasswordHash)) {
            log.warn("Intento de reutilizar password anterior para usuario: {}", usuarioId);
            throw new PasswordReutilizadaException(
                    "No puedes reutilizar ninguna de tus últimas 5 contraseñas"
            );
        }

        passwordHistoryRepository.guardar(userUuid, usuario.passwordHash());
        usuarioRepository.actualizarIntentosFallidos(userUuid, 0, null);

        Usuario usuarioActualizado = usuario.conPasswordCambiado(nuevoPasswordHash);

        usuarioRepository.actualizar(usuarioActualizado);

        log.info("Password cambiada exitosamente para usuario: {}", usuarioId);

        return new MensajeResponseDTO("Contraseña actualizada exitosamente");
    }

    private boolean esPasswordValida(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }
}