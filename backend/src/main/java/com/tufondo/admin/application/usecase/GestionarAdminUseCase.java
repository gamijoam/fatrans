package com.tufondo.admin.application.usecase;

import com.tufondo.admin.application.dto.AdminUsuarioRequest;
import com.tufondo.admin.application.dto.AdminUsuarioResponse;
import com.tufondo.creditos.infrastructure.security.XssSanitizer;
import com.tufondo.auth.domain.model.Usuario;
import com.tufondo.auth.domain.model.enums.Rol;
import com.tufondo.auth.domain.repository.UsuarioRepository;
import com.tufondo.auth.infrastructure.service.Argon2Hasher;
import com.tufondo.auth.infrastructure.service.SecurityAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GestionarAdminUseCase {

    private final UsuarioRepository usuarioRepository;
    private final Argon2Hasher argon2Hasher;
    private final XssSanitizer xssSanitizer;
    private final SecurityAuditService auditService;

    @Transactional(readOnly = true)
    public List<AdminUsuarioResponse> listarAdmins() {
        return usuarioRepository.listarPorRol(Rol.ADMIN).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AdminUsuarioResponse> listarTodos() {
        return usuarioRepository.listarTodos().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AdminUsuarioResponse obtenerPorId(UUID id) {
        Usuario usuario = usuarioRepository.buscarPorId(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));
        return toResponse(usuario);
    }

    @Transactional
    public AdminUsuarioResponse crear(AdminUsuarioRequest request, UUID adminCreadorId, String ipAddress) {
        String nombreUsuarioSanitizado = xssSanitizer.sanitizeUsername(request.getNombreUsuario());
        String correoSanitizado = xssSanitizer.sanitize(request.getCorreoElectronico()).toLowerCase().trim();
        String nombreCompletoSanitizado = xssSanitizer.sanitize(request.getNombreCompleto());

        if (usuarioRepository.existePorNombreUsuario(nombreUsuarioSanitizado)) {
            throw new NombreUsuarioYaExisteException(nombreUsuarioSanitizado);
        }

        if (usuarioRepository.existePorCorreoElectronico(correoSanitizado)) {
            throw new CorreoYaExisteException(correoSanitizado);
        }

        String passwordHash = argon2Hasher.hash(request.getPassword());

        Rol rol;
        try {
            rol = Rol.valueOf(request.getRol().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RolInvalidoException(request.getRol());
        }

        if (rol != Rol.ADMIN && rol != Rol.SUPER_ADMIN) {
            throw new RolInvalidoException("Solo se permiten ADMIN o SUPER_ADMIN");
        }

        Usuario nuevoUsuario = Usuario.crear(
                nombreUsuarioSanitizado,
                correoSanitizado,
                passwordHash,
                nombreCompletoSanitizado,
                rol,
                null
        );

        usuarioRepository.guardar(nuevoUsuario);

        auditService.logEntityEvent("ADMIN_CREADO", adminCreadorId, ipAddress, "USUARIO",
                nuevoUsuario.id().toString(), "ADMIN_CREADO", "Admin creado: " + nombreUsuarioSanitizado + ", rol: " + rol);

        return toResponse(nuevoUsuario);
    }

    @Transactional
    public AdminUsuarioResponse actualizar(UUID id, AdminUsuarioRequest request, UUID adminActualizadorId, String ipAddress) {
        Usuario usuario = usuarioRepository.buscarPorId(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));

        String nombreUsuarioSanitizado = xssSanitizer.sanitizeUsername(request.getNombreUsuario());
        String correoSanitizado = xssSanitizer.sanitize(request.getCorreoElectronico()).toLowerCase().trim();
        String nombreCompletoSanitizado = xssSanitizer.sanitize(request.getNombreCompleto());

        if (!usuario.nombreUsuario().equals(nombreUsuarioSanitizado) &&
            usuarioRepository.existePorNombreUsuario(nombreUsuarioSanitizado)) {
            throw new NombreUsuarioYaExisteException(nombreUsuarioSanitizado);
        }

        if (!usuario.correoElectronico().equals(correoSanitizado) &&
            usuarioRepository.existePorCorreoElectronico(correoSanitizado)) {
            throw new CorreoYaExisteException(correoSanitizado);
        }

        Rol rol;
        try {
            rol = Rol.valueOf(request.getRol().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RolInvalidoException(request.getRol());
        }

        if (rol != Rol.ADMIN && rol != Rol.SUPER_ADMIN) {
            throw new RolInvalidoException("Solo se permiten ADMIN o SUPER_ADMIN");
        }

        String passwordHash = null;
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            passwordHash = argon2Hasher.hash(request.getPassword());
        }

        Usuario usuarioActualizado = Usuario.desdeParametros(
                usuario.id(),
                nombreUsuarioSanitizado,
                correoSanitizado,
                passwordHash != null ? passwordHash : usuario.passwordHash(),
                nombreCompletoSanitizado,
                rol,
                usuario.socioId(),
                request.getCuentaActiva() != null ? request.getCuentaActiva() : usuario.cuentaActiva(),
                usuario.fechaCreacion(),
                Instant.now(),
                usuario.intentosFallidos(),
                usuario.fechaBloqueo(),
                true
        );

        usuarioRepository.actualizar(usuarioActualizado);

        auditService.logEntityEvent("ADMIN_ACTUALIZADO", adminActualizadorId, ipAddress, "USUARIO",
                id.toString(), "ADMIN_ACTUALIZADO", "Admin actualizado: " + nombreUsuarioSanitizado);

        return toResponse(usuarioActualizado);
    }

    @Transactional
    public AdminUsuarioResponse activar(UUID id, UUID adminActivadorId, String ipAddress) {
        Usuario usuario = usuarioRepository.buscarPorId(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));

        Usuario usuarioActivado = Usuario.desdeParametros(
                usuario.id(),
                usuario.nombreUsuario(),
                usuario.correoElectronico(),
                usuario.passwordHash(),
                usuario.nombreCompleto(),
                usuario.rol(),
                usuario.socioId(),
                true,
                usuario.fechaCreacion(),
                Instant.now(),
                0,
                null,
                true
        );

        usuarioRepository.actualizar(usuarioActivado);

        auditService.logEntityEvent("ADMIN_ACTIVADO", adminActivadorId, ipAddress, "USUARIO",
                id.toString(), "ADMIN_ACTIVADO", "Admin activado: " + usuario.nombreUsuario());

        return toResponse(usuarioActivado);
    }

    @Transactional
    public AdminUsuarioResponse desactivar(UUID id, UUID adminDesactivadorId, String ipAddress) {
        Usuario usuario = usuarioRepository.buscarPorId(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));

        if (usuario.rol() == Rol.SUPER_ADMIN) {
            throw new NoSePuedeDesactivarSuperAdminException();
        }

        Usuario usuarioFinal = Usuario.desdeParametros(
                usuario.id(),
                usuario.nombreUsuario(),
                usuario.correoElectronico(),
                usuario.passwordHash(),
                usuario.nombreCompleto(),
                usuario.rol(),
                usuario.socioId(),
                false,
                usuario.fechaCreacion(),
                Instant.now(),
                usuario.intentosFallidos(),
                usuario.fechaBloqueo(),
                usuario.debeCambiarPassword()
        );

        usuarioRepository.actualizar(usuarioFinal);

        auditService.logEntityEvent("ADMIN_DESACTIVADO", adminDesactivadorId, ipAddress, "USUARIO",
                id.toString(), "ADMIN_DESACTIVADO", "Admin desactivado: " + usuario.nombreUsuario());

        return toResponse(usuarioFinal);
    }

    private AdminUsuarioResponse toResponse(Usuario usuario) {
        return AdminUsuarioResponse.builder()
                .id(usuario.id())
                .nombreUsuario(usuario.nombreUsuario())
                .correoElectronico(usuario.correoElectronico())
                .nombreCompleto(usuario.nombreCompleto())
                .rol(usuario.rol().name())
                .cuentaActiva(usuario.cuentaActiva())
                .fechaCreacion(usuario.fechaCreacion())
                .ultimaModificacion(usuario.ultimaModificacion())
                .debeCambiarPassword(usuario.debeCambiarPassword())
                .build();
    }

    public static class UsuarioNoEncontradoException extends RuntimeException {
        public UsuarioNoEncontradoException(UUID id) {
            super("Usuario no encontrado con ID: " + id);
        }
    }

    public static class NombreUsuarioYaExisteException extends RuntimeException {
        public NombreUsuarioYaExisteException(String nombreUsuario) {
            super("Ya existe un usuario con nombre de usuario: " + nombreUsuario);
        }
    }

    public static class CorreoYaExisteException extends RuntimeException {
        public CorreoYaExisteException(String correo) {
            super("Ya existe un usuario con correo electrónico: " + correo);
        }
    }

    public static class RolInvalidoException extends RuntimeException {
        public RolInvalidoException(String rol) {
            super("Rol inválido: " + rol);
        }
    }

    public static class NoSePuedeDesactivarSuperAdminException extends RuntimeException {
        public NoSePuedeDesactivarSuperAdminException() {
            super("No se puede desactivar un usuario SUPER_ADMIN");
        }
    }
}