package com.tufondo.admin.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUsuarioResponse {
    private UUID id;
    private String nombreUsuario;
    private String correoElectronico;
    private String nombreCompleto;
    private String rol;
    private boolean cuentaActiva;
    private Instant fechaCreacion;
    private Instant ultimaModificacion;
    private boolean debeCambiarPassword;
}