package com.tufondo.auth.domain.repository;

import com.tufondo.auth.domain.model.Usuario;
import com.tufondo.auth.domain.model.enums.Rol;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository {

    Optional<Usuario> buscarPorId(UUID id);

    Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario);

    Optional<Usuario> buscarPorCorreoElectronico(String correo);

    Optional<Usuario> buscarPorSocioId(UUID socioId);

    boolean existePorNombreUsuario(String nombreUsuario);

    boolean existePorCorreoElectronico(String correo);

    boolean existePorSocioId(UUID socioId);

    void guardar(Usuario usuario);

    void actualizar(Usuario usuario);

    void actualizarIntentosFallidos(UUID usuarioId, int intentos, java.time.Instant fechaBloqueo);

    void resetearIntentosFallidos(UUID usuarioId);

    Optional<Usuario> buscarAdminActivo();

    Optional<Usuario> buscarSocioActivoPorSocioId(UUID socioId);
}
