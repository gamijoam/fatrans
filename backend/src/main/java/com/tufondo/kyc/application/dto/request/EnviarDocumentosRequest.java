// com.tufondo.kyc.application.dto.request.EnviarDocumentosRequest
package com.tufondo.kyc.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO para enviar documentos a revision.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnviarDocumentosRequest {

    @NotNull(message = "verificacionId es requerido")
    private UUID verificacionId;
}