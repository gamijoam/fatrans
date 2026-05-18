package com.tufondo.admin.application.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUsuarioRequest {

    @NotBlank(message = "El nombre de usuario es requerido")
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
    private String nombreUsuario;

    @NotBlank(message = "El correo electrónico es requerido")
    @Email(message = "El correo electrónico debe ser válido")
    @Size(max = 255, message = "El correo no puede exceder 255 caracteres")
    private String correoElectronico;

    @NotBlank(message = "El nombre completo es requerido")
    @Size(max = 200, message = "El nombre completo no puede exceder 200 caracteres")
    private String nombreCompleto;

    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    @NotNull(message = "El rol es requerido")
    private String rol;

    private Boolean cuentaActiva = true;
}