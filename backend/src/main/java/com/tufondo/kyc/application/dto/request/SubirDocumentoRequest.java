// com.tufondo.kyc.application.dto.request.SubirDocumentoRequest
package com.tufondo.kyc.application.dto.request;

import com.tufondo.kyc.domain.model.enums.TipoDocumentoKYC;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO para subir un documento.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubirDocumentoRequest {

    @NotNull(message = "verificacionId es requerido")
    private UUID verificacionId;

    @NotNull(message = "tipoDocumento es requerido")
    private TipoDocumentoKYC tipoDocumento;

    @NotBlank(message = "archivoBase64 es requerido")
    private String archivoBase64;

    @NotBlank(message = "nombreOriginal es requerido")
    @Size(max = 255, message = "Nombre de archivo muy largo (max 255 caracteres)")
    @Pattern(regexp = "^[^./\\\\]+$", message = "Nombre de archivo invalido (no se permiten rutas ni ../)")
    private String nombreOriginal;

    @NotNull(message = "tamanoBytes es requerido")
    private Long tamanoBytes;

    @NotBlank(message = "mimeType es requerido")
    private String mimeType;

    private LocalDate fechaExpiracionDocumento;
}