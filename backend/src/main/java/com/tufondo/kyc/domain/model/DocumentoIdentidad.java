// com.tufondo.kyc.domain.model.DocumentoIdentidad
package com.tufondo.kyc.domain.model;

import com.tufondo.kyc.domain.model.enums.EstadoDocumento;
import com.tufondo.kyc.domain.model.enums.TipoDocumentoKYC;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad de dominio para DocumentoIdentidad.
 */
@Getter
@Setter
@Builder
public class DocumentoIdentidad {

    private UUID id;
    private UUID verificacionId;
    private UUID socioId;
    private TipoDocumentoKYC tipoDocumento;
    private String urlAlmacenamiento;
    private String nombreOriginal;
    private Long tamanoBytes;
    private String mimeType;
    private String hashArchivo;
    private LocalDateTime fechaSubida;
    private LocalDate fechaExpiracionDocumento;
    private EstadoDocumento estado;
    private String motivoRechazo;
    private String metadatosValidacion;
    private String observaciones;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean estaValido() {
        return this.estado == EstadoDocumento.VALIDADO;
    }

    public boolean estaPendiente() {
        return this.estado == EstadoDocumento.PENDIENTE;
    }

    public boolean estaRechazado() {
        return this.estado == EstadoDocumento.RECHAZADO;
    }

    public boolean puedeSerEliminado() {
        return this.estado == EstadoDocumento.PENDIENTE;
    }

    public void marcarComoRechazado(String motivo) {
        this.estado = EstadoDocumento.RECHAZADO;
        this.motivoRechazo = motivo;
        this.updatedAt = LocalDateTime.now();
    }

    public void marcarComoValidado() {
        this.estado = EstadoDocumento.VALIDADO;
        this.motivoRechazo = null;
        this.updatedAt = LocalDateTime.now();
    }
}