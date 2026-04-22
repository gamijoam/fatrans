// com.tufondo.documentospdf.application.dto.DocumentoListResponseDTO
package com.tufondo.documentospdf.application.dto;

import com.tufondo.documentospdf.domain.model.enums.ClasificacionDocumento;
import com.tufondo.documentospdf.domain.model.enums.EstadoDocumento;
import com.tufondo.documentospdf.domain.model.enums.TipoDocumento;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para listar documentos de un socio.
 */
public record DocumentoListResponseDTO(
    UUID documentoId,
    TipoDocumento tipo,
    String nombreArchivo,
    EstadoDocumento estado,
    ClasificacionDocumento clasificacion,
    LocalDateTime fechaGeneracion
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID documentoId;
        private TipoDocumento tipo;
        private String nombreArchivo;
        private EstadoDocumento estado;
        private ClasificacionDocumento clasificacion;
        private LocalDateTime fechaGeneracion;

        public Builder documentoId(UUID documentoId) { this.documentoId = documentoId; return this; }
        public Builder tipo(TipoDocumento tipo) { this.tipo = tipo; return this; }
        public Builder nombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; return this; }
        public Builder estado(EstadoDocumento estado) { this.estado = estado; return this; }
        public Builder clasificacion(ClasificacionDocumento clasificacion) { this.clasificacion = clasificacion; return this; }
        public Builder fechaGeneracion(LocalDateTime fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; return this; }

        public DocumentoListResponseDTO build() {
            return new DocumentoListResponseDTO(documentoId, tipo, nombreArchivo, estado, clasificacion, fechaGeneracion);
        }
    }
}
