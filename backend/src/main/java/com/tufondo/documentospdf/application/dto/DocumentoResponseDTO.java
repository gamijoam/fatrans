// com.tufondo.documentospdf.application.dto.DocumentoResponseDTO
package com.tufondo.documentospdf.application.dto;

import com.tufondo.documentospdf.domain.model.enums.ClasificacionDocumento;
import com.tufondo.documentospdf.domain.model.enums.EstadoDocumento;
import com.tufondo.documentospdf.domain.model.enums.TipoDocumento;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para operaciones con documentos PDF.
 * Utiliza Java record para inmutabilidad.
 */
public record DocumentoResponseDTO(
    UUID documentoId,
    UUID socioId,
    TipoDocumento tipo,
    String nombreArchivo,
    EstadoDocumento estado,
    Long tamanoBytes,
    String hashArchivo,
    ClasificacionDocumento clasificacion,
    String firmaDigital,
    String preSignedUrl,
    Integer urlExpiraEn,
    LocalDateTime fechaGeneracion,
    LocalDateTime fechaExpiracion
) {
    /**
     * Builder estático para crear instancias.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID documentoId;
        private UUID socioId;
        private TipoDocumento tipo;
        private String nombreArchivo;
        private EstadoDocumento estado;
        private Long tamanoBytes;
        private String hashArchivo;
        private ClasificacionDocumento clasificacion;
        private String firmaDigital;
        private String preSignedUrl;
        private Integer urlExpiraEn;
        private LocalDateTime fechaGeneracion;
        private LocalDateTime fechaExpiracion;

        public Builder documentoId(UUID documentoId) { this.documentoId = documentoId; return this; }
        public Builder socioId(UUID socioId) { this.socioId = socioId; return this; }
        public Builder tipo(TipoDocumento tipo) { this.tipo = tipo; return this; }
        public Builder nombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; return this; }
        public Builder estado(EstadoDocumento estado) { this.estado = estado; return this; }
        public Builder tamanoBytes(Long tamanoBytes) { this.tamanoBytes = tamanoBytes; return this; }
        public Builder hashArchivo(String hashArchivo) { this.hashArchivo = hashArchivo; return this; }
        public Builder clasificacion(ClasificacionDocumento clasificacion) { this.clasificacion = clasificacion; return this; }
        public Builder firmaDigital(String firmaDigital) { this.firmaDigital = firmaDigital; return this; }
        public Builder preSignedUrl(String preSignedUrl) { this.preSignedUrl = preSignedUrl; return this; }
        public Builder urlExpiraEn(Integer urlExpiraEn) { this.urlExpiraEn = urlExpiraEn; return this; }
        public Builder fechaGeneracion(LocalDateTime fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; return this; }
        public Builder fechaExpiracion(LocalDateTime fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; return this; }

        public DocumentoResponseDTO build() {
            return new DocumentoResponseDTO(
                documentoId, socioId, tipo, nombreArchivo, estado, tamanoBytes,
                hashArchivo, clasificacion, firmaDigital, preSignedUrl,
                urlExpiraEn, fechaGeneracion, fechaExpiracion
            );
        }
    }
}
