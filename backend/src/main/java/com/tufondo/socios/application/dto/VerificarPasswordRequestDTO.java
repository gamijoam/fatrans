package com.tufondo.socios.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificarPasswordRequestDTO {

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}