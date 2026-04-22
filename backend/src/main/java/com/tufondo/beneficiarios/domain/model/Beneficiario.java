// com/tufondo/beneficiarios/domain/model/Beneficiario.java
package com.tufondo.beneficiarios.domain.model;

import com.tufondo.beneficiarios.domain.model.enums.Parentesco;
import com.tufondo.beneficiarios.domain.model.enums.TipoDocumento;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entidad Beneficiario - Representa un beneficiario designado por un socio.
 * Un beneficiario es la persona designada para recibir los fondos en caso de fallecimiento.
 */
@Getter
@Setter
@Builder
public class Beneficiario {

    private UUID id;
    private UUID socioId;
    private String nombreCompleto;
    private String numeroDocumento;
    private TipoDocumento tipoDocumento;
    private Parentesco parentesco;
    private BigDecimal porcentaje;
    private String telefono;
    private Instant fechaRegistro;
    private Instant fechaActualizacion;
    private boolean activo;

    /**
     * Método de fábrica para crear un nuevo beneficiario.
     */
    public static Beneficiario crear(UUID socioId, String nombreCompleto, String numeroDocumento,
                                     TipoDocumento tipoDocumento, Parentesco parentesco,
                                     BigDecimal porcentaje, String telefono) {
        Instant now = Instant.now();
        return Beneficiario.builder()
                .socioId(socioId)
                .nombreCompleto(nombreCompleto)
                .numeroDocumento(numeroDocumento)
                .tipoDocumento(tipoDocumento)
                .parentesco(parentesco)
                .porcentaje(porcentaje)
                .telefono(telefono)
                .fechaRegistro(now)
                .fechaActualizacion(now)
                .activo(true)
                .build();
    }

    /**
     * Actualiza la fecha de modificación.
     */
    public void conActualizacion() {
        this.fechaActualizacion = Instant.now();
    }

    /**
     * Marca el beneficiario como inactivo (soft delete).
     */
    public void marcarInactivo() {
        this.activo = false;
        this.fechaActualizacion = Instant.now();
    }

    /**
     * Verifica si el beneficiario está activo.
     */
    public boolean estaActivo() {
        return this.activo;
    }

    /**
     * Valida que el porcentaje esté en rango válido (0.01 - 100.00).
     */
    public boolean tienePorcentajeValido() {
        if (porcentaje == null) return false;
        return porcentaje.compareTo(BigDecimal.valueOf(0.01)) >= 0
            && porcentaje.compareTo(BigDecimal.valueOf(100.00)) <= 0;
    }
}