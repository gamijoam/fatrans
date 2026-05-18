package com.tufondo.socios.application.dto;

import com.tufondo.socios.domain.model.enums.TipoVerificacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmarCodigoRequestDTO {

    @NotNull(message = "El tipo de verificación es obligatorio")
    private TipoVerificacion tipo;

    @NotBlank(message = "El token es obligatorio")
    private String token;

    @NotBlank(message = "El código es obligatorio")
    private String codigo;
}