// com.tufondo.kyc.application.dto.request.AprobarVerificacionRequest
package com.tufondo.kyc.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para aprobar una verificacion.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AprobarVerificacionRequest {

    private String comentario;
}