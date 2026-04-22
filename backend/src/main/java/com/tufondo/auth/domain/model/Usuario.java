package com.tufondo.auth.domain.model;

import com.tufondo.auth.domain.model.enums.Rol;
import java.time.Instant;
import java.util.UUID;

public final class Usuario {

    private final UUID id;
    private final String nombreUsuario;
    private final String correoElectronico;
    private final String passwordHash;
    private final String nombreCompleto;
    private final Rol rol;
    private final UUID socioId;
    private final boolean cuentaActiva;
    private final Instant fechaCreacion;
    private final Instant ultimaModificacion;
    private final int intentosFallidos;
    private final Instant fechaBloqueo;

    private Usuario(UUID id, String nombreUsuario, String correoElectronico,
                    String passwordHash, String nombreCompleto, Rol rol,
                    UUID socioId, boolean cuentaActiva, Instant fechaCreacion,
                    Instant ultimaModificacion, int intentosFallidos, Instant fechaBloqueo) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.correoElectronico = correoElectronico;
        this.passwordHash = passwordHash;
        this.nombreCompleto = nombreCompleto;
        this.rol = rol;
        this.socioId = socioId;
        this.cuentaActiva = cuentaActiva;
        this.fechaCreacion = fechaCreacion;
        this.ultimaModificacion = ultimaModificacion;
        this.intentosFallidos = intentosFallidos;
        this.fechaBloqueo = fechaBloqueo;
    }

    public static Usuario crear(String nombreUsuario, String correoElectronico,
                                String passwordHash, String nombreCompleto,
                                Rol rol, UUID socioId) {
        return new Usuario(
            UUID.randomUUID(),
            nombreUsuario,
            correoElectronico,
            passwordHash,
            nombreCompleto,
            rol,
            socioId,
            true,
            Instant.now(),
            null,
            0,
            null
        );
    }

    public static Usuario desdeParametros(UUID id, String nombreUsuario, String correoElectronico,
                                          String passwordHash, String nombreCompleto, Rol rol,
                                          UUID socioId, boolean cuentaActiva, Instant fechaCreacion,
                                          Instant ultimaModificacion, int intentosFallidos, Instant fechaBloqueo) {
        return new Usuario(id, nombreUsuario, correoElectronico, passwordHash, nombreCompleto,
                          rol, socioId, cuentaActiva, fechaCreacion, ultimaModificacion,
                          intentosFallidos, fechaBloqueo);
    }

    public Usuario conIntentosFallidos(int intentos, Instant fechaBloqueo) {
        return new Usuario(id, nombreUsuario, correoElectronico, passwordHash, nombreCompleto,
                          rol, socioId, cuentaActiva, fechaCreacion, Instant.now(),
                          intentos, fechaBloqueo);
    }

    public Usuario conIntentosReseteados() {
        return new Usuario(id, nombreUsuario, correoElectronico, passwordHash, nombreCompleto,
                          rol, socioId, cuentaActiva, fechaCreacion, Instant.now(),
                          0, null);
    }

    public Usuario conCuentaDesactivada() {
        return new Usuario(id, nombreUsuario, correoElectronico, passwordHash, nombreCompleto,
                          rol, socioId, false, fechaCreacion, Instant.now(),
                          intentosFallidos, fechaBloqueo);
    }

    public UUID id() { return id; }
    public String nombreUsuario() { return nombreUsuario; }
    public String correoElectronico() { return correoElectronico; }
    public String passwordHash() { return passwordHash; }
    public String nombreCompleto() { return nombreCompleto; }
    public Rol rol() { return rol; }
    public UUID socioId() { return socioId; }
    public boolean cuentaActiva() { return cuentaActiva; }
    public Instant fechaCreacion() { return fechaCreacion; }
    public Instant ultimaModificacion() { return ultimaModificacion; }
    public int intentosFallidos() { return intentosFallidos; }
    public Instant fechaBloqueo() { return fechaBloqueo; }
}
