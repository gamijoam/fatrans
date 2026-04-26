package com.tufondo.socios.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmarCodigoResponseDTO {

    private boolean valido;
    private String tokenVerificacion;
}