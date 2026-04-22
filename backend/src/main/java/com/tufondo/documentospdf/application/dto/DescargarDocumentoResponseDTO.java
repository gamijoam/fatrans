// com.tufondo.documentospdf.application.dto.DescargarDocumentoResponseDTO
package com.tufondo.documentospdf.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para la respuesta de descarga de documento.
 * Contiene la pre-signed URL para acceder al PDF en MinIO.
 */
public record DescargarDocumentoResponseDTO(
    UUID documentoId,
    String preSignedUrl,
    Integer urlExpiraEn,
    LocalDateTime fechaExpiracion
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID documentoId;
        private String preSignedUrl;
        private Integer urlExpiraEn;
        private LocalDateTime fechaExpiracion;

        public Builder documentoId(UUID documentoId) { this.documentoId = documentoId; return this; }
        public Builder preSignedUrl(String preSignedUrl) { this.preSignedUrl = preSignedUrl; return this; }
        public Builder urlExpiraEn(Integer urlExpiraEn) { this.urlExpiraEn = urlExpiraEn; return this; }
        public Builder fechaExpiracion(LocalDateTime fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; return this; }

        public DescargarDocumentoResponseDTO build() {
            return new DescargarDocumentoResponseDTO(documentoId, preSignedUrl, urlExpiraEn, fechaExpiracion);
        }
    }
}
