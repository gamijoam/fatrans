// com.tufondo.kyc.domain.model.VerificacionKYC
package com.tufondo.kyc.domain.model;

import com.tufondo.kyc.domain.model.enums.EstadoBiometria;
import com.tufondo.kyc.domain.model.enums.EstadoVerificacion;
import com.tufondo.kyc.domain.model.enums.NivelVerificacion;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad de dominio para VerificacionKYC.
 */
@Getter
@Setter
@Builder
public class VerificacionKYC {

    private UUID id;
    private UUID socioId;
    private NivelVerificacion nivel;
    private EstadoVerificacion estado;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaCompletado;
    private LocalDateTime fechaExpiracion;
    private String datosVerificacionAutomatica;
    private String revisadoPor;
    private LocalDateTime fechaRevision;
    private String comentariosRevision;
    private String motivoRechazo;
    @Builder.Default
    private List<DocumentoIdentidad> documentos = new ArrayList<>();
    /**
     * Cache del resultado del flujo biométrico (Didit/AWS) — null en KYCs creadas antes
     * de la migración V13. Lo mantiene sincronizado el use case que procesa el webhook.
     */
    @Builder.Default
    private EstadoBiometria estadoBiometria = EstadoBiometria.NO_INICIADA;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean estaPendiente() {
        return this.estado == EstadoVerificacion.PENDIENTE;
    }

    public boolean puedeSerRevisada() {
        return this.estado == EstadoVerificacion.EN_REVISION;
    }

    public boolean estaAprobada() {
        return this.estado == EstadoVerificacion.APROBADO;
    }

    public boolean estaRechazada() {
        return this.estado == EstadoVerificacion.RECHAZADO;
    }

    public boolean estaExpirada() {
        return this.estado == EstadoVerificacion.EXPIRADO
            || (this.fechaExpiracion != null && this.fechaExpiracion.isBefore(LocalDateTime.now()));
    }

    public boolean puedeRenovarse() {
        return this.estado == EstadoVerificacion.EXPIRADO
            || this.estado == EstadoVerificacion.RECHAZADO;
    }

    public boolean esEditable() {
        return this.estado.esEditable();
    }

    public void agregarDocumento(DocumentoIdentidad documento) {
        this.documentos.add(documento);
    }

    public int getDocumentosRequeridos() {
        return this.nivel.getCantidadDocumentosRequeridos();
    }

    public int getDocumentosValidos() {
        return (int) this.documentos.stream()
            .filter(doc -> doc.estaValido())
            .count();
    }

    public int getDocumentosSubidos() {
        return this.documentos.size();
    }
}