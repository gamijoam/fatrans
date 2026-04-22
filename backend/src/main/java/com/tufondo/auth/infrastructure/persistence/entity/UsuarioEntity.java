package com.tufondo.auth.infrastructure.persistence.entity;

import com.tufondo.auth.domain.model.Usuario;
import com.tufondo.auth.domain.model.enums.Rol;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nombre_usuario", nullable = false, unique = true, length = 50)
    private String nombreUsuario;

    @Column(name = "correo_electronico", nullable = false, unique = true, length = 255)
    private String correoElectronico;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "nombre_completo", nullable = false, length = 200)
    private String nombreCompleto;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 20)
    private Rol rol;

    @Column(name = "socio_id")
    private UUID socioId;

    @Column(name = "cuenta_activa", nullable = false)
    private boolean cuentaActiva;

    @Column(name = "fecha_creacion", nullable = false)
    private Instant fechaCreacion;

    @Column(name = "ultima_modificacion")
    private Instant ultimaModificacion;

    @Column(name = "intentos_fallidos", nullable = false)
    private int intentosFallidos;

    @Column(name = "fecha_bloqueo")
    private Instant fechaBloqueo;

    public UsuarioEntity() {}

    public static UsuarioEntity desdeDominio(Usuario usuario) {
        UsuarioEntity entity = new UsuarioEntity();
        entity.id = usuario.id();
        entity.nombreUsuario = usuario.nombreUsuario();
        entity.correoElectronico = usuario.correoElectronico();
        entity.passwordHash = usuario.passwordHash();
        entity.nombreCompleto = usuario.nombreCompleto();
        entity.rol = usuario.rol();
        entity.socioId = usuario.socioId();
        entity.cuentaActiva = usuario.cuentaActiva();
        entity.fechaCreacion = usuario.fechaCreacion();
        entity.ultimaModificacion = usuario.ultimaModificacion();
        entity.intentosFallidos = usuario.intentosFallidos();
        entity.fechaBloqueo = usuario.fechaBloqueo();
        return entity;
    }

    public Usuario aDominio() {
        return Usuario.desdeParametros(
            id, nombreUsuario, correoElectronico, passwordHash, nombreCompleto,
            rol, socioId, cuentaActiva, fechaCreacion, ultimaModificacion,
            intentosFallidos, fechaBloqueo
        );
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
    public String getCorreoElectronico() { return correoElectronico; }
    public void setCorreoElectronico(String correoElectronico) { this.correoElectronico = correoElectronico; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    public UUID getSocioId() { return socioId; }
    public void setSocioId(UUID socioId) { this.socioId = socioId; }
    public boolean isCuentaActiva() { return cuentaActiva; }
    public void setCuentaActiva(boolean cuentaActiva) { this.cuentaActiva = cuentaActiva; }
    public Instant getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Instant fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public Instant getUltimaModificacion() { return ultimaModificacion; }
    public void setUltimaModificacion(Instant ultimaModificacion) { this.ultimaModificacion = ultimaModificacion; }
    public int getIntentosFallidos() { return intentosFallidos; }
    public void setIntentosFallidos(int intentosFallidos) { this.intentosFallidos = intentosFallidos; }
    public Instant getFechaBloqueo() { return fechaBloqueo; }
    public void setFechaBloqueo(Instant fechaBloqueo) { this.fechaBloqueo = fechaBloqueo; }
}
