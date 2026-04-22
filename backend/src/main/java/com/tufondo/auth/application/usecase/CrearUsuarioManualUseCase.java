package com.tufondo.auth.application.usecase;

import com.tufondo.auth.application.dto.CrearUsuarioRequestDTO;
import com.tufondo.auth.application.dto.CrearUsuarioResponseDTO;
import com.tufondo.auth.domain.exception.NombreUsuarioYaExisteException;
import com.tufondo.auth.domain.exception.SocioYaTieneUsuarioException;
import com.tufondo.auth.domain.model.Usuario;
import com.tufondo.auth.domain.model.enums.Rol;
import com.tufondo.auth.domain.repository.UsuarioRepository;
import com.tufondo.socios.domain.exception.SocioNoEncontradoException;
import com.tufondo.socios.domain.model.Socio;
import com.tufondo.socios.domain.repository.SocioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Caso de uso para crear un usuario manualmente vinculado a un socio existente.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CrearUsuarioManualUseCase {

    private final UsuarioRepository usuarioRepository;
    private final SocioRepository socioRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Crea un usuario vinculado a un socio existente.
     *
     * @param request DTO con los datos del usuario a crear
     * @return Response con el ID del usuario creado y mensaje de éxito
     * @throws SocioNoEncontradoException si el socio no existe
     * @throws SocioYaTieneUsuarioException si el socio ya tiene un usuario vinculado
     * @throws NombreUsuarioYaExisteException si el nombre de usuario ya está en uso
     */
    @Transactional
    public CrearUsuarioResponseDTO ejecutar(CrearUsuarioRequestDTO request) {
        log.info("Solicitud de creación de usuario para socioId: {}", request.socioId());

        // 1. Validar que el socio existe
        UUID socioId;
        try {
            socioId = UUID.fromString(request.socioId());
        } catch (IllegalArgumentException e) {
            throw new SocioNoEncontradoException("ID de socio inválido: " + request.socioId());
        }

        Optional<Socio> socioOpt = socioRepository.buscarPorId(socioId);
        if (socioOpt.isEmpty()) {
            log.warn("Socio no encontrado: {}", socioId);
            throw new SocioNoEncontradoException("Socio no encontrado con ID: " + socioId);
        }

        Socio socio = socioOpt.get();

        // 2. Validar que el socio no tiene usuario ya vinculado
        if (usuarioRepository.existePorSocioId(socioId)) {
            log.warn("Socio {} ya tiene usuario vinculado", socioId);
            throw new SocioYaTieneUsuarioException("El socio ya tiene un usuario vinculado");
        }

        // 3. Validar que el nombre de usuario no existe
        if (usuarioRepository.existePorNombreUsuario(request.nombreUsuario())) {
            log.warn("Nombre de usuario ya existe: {}", request.nombreUsuario());
            throw new NombreUsuarioYaExisteException(request.nombreUsuario());
        }

        // 4. Construir nombre completo del socio
        String nombreCompleto = construirNombreCompleto(socio);

        // 5. Crear el usuario con password codificada
        String passwordHash = passwordEncoder.encode(request.password());

        Usuario nuevoUsuario = Usuario.crear(
                request.nombreUsuario(),
                socio.getCorreoElectronico(),
                passwordHash,
                nombreCompleto,
                Rol.SOCIO,
                socioId
        );

        // 6. Guardar el usuario
        usuarioRepository.guardar(nuevoUsuario);

        log.info("Usuario creado exitosamente: {} para socio: {}", 
                nuevoUsuario.id(), socioId);

        return new CrearUsuarioResponseDTO(
                nuevoUsuario.id().toString(),
                nuevoUsuario.nombreUsuario(),
                "Usuario creado exitosamente"
        );
    }

    /**
     * Construye el nombre completo a partir de los datos del socio.
     */
    private String construirNombreCompleto(Socio socio) {
        StringBuilder sb = new StringBuilder();
        
        if (socio.getPrimerNombre() != null) {
            sb.append(socio.getPrimerNombre());
        }
        if (socio.getSegundoNombre() != null && !socio.getSegundoNombre().isEmpty()) {
            sb.append(" ").append(socio.getSegundoNombre());
        }
        if (socio.getPrimerApellido() != null) {
            sb.append(" ").append(socio.getPrimerApellido());
        }
        if (socio.getSegundoApellido() != null && !socio.getSegundoApellido().isEmpty()) {
            sb.append(" ").append(socio.getSegundoApellido());
        }
        
        return sb.toString().trim();
    }
}
