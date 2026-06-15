// com/tufondo/creditos/domain/model/SolicitudCredito.java
package com.tufondo.creditos.domain.model;

import com.tufondo.creditos.domain.model.enums.EstadoSolicitud;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad SolicitudCredito - Solicitud de crédito de un socio.
 * El número de solicitud es no enumerable usando SecureRandom.
 */
@Getter
@Setter
@Builder
public class SolicitudCredito {
    private UUID id;
    private String numeroSolicitud;  // Formato: SOL-CRED-YYYY-XXXXXX (SecureRandom)
    private UUID socioId;
    private Long tipoCreditoId;
    private TipoCredito tipoCredito;  // Relación lazy
    private BigDecimal montoSolicitado;
    private Integer plazoMeses;
    private BigDecimal tasaInteresAplicada;
    private BigDecimal cuotaMensualEstimada;
    private EstadoSolicitud estado;
    private UUID colateralCuentaId;  // FK a cuenta de ahorro (nullable)
    private BigDecimal colateralMontoRetenido;
    private String destinoCredito;
    private Long productoFinanciableId;
    private String productoNombreSnapshot;
    private BigDecimal productoPrecioSnapshot;
    private String productoMonedaSnapshot;
    private BigDecimal productoColateralRequeridoSnapshot;
    private UUID evaluacionId;
    private UUID planAmortizacionId;
    private String referenciaDesembolso;
    private String cuentaDestino;  // IBAN o número de cuenta
    private String notas;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;

    /**
     * Valida si la solicitud puede transicionar al nuevo estado.
     */
    public boolean puedeTransicionarA(EstadoSolicitud nuevoEstado) {
        return estado.puedeTransicionarA(nuevoEstado);
    }

    /**
     * Transiciona al nuevo estado si es válido.
     * @throws IllegalStateException si la transición no es válida
     */
    public void transicionarA(EstadoSolicitud nuevoEstado) {
        if (!puedeTransicionarA(nuevoEstado)) {
            throw new IllegalStateException(
                String.format("Transición inválida: %s → %s", estado, nuevoEstado)
            );
        }
        this.estado = nuevoEstado;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Verifica si la solicitud tiene colateral asignado.
     */
    public boolean tieneColateral() {
        return colateralCuentaId != null && colateralMontoRetenido != null 
            && colateralMontoRetenido.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Verifica si el socio tiene un crédito activo (en estado DESEMBOLSADO).
     */
    public boolean esCreditoActivo() {
        return estado == EstadoSolicitud.DESEMBOLSADO;
    }

    /**
     * Verifica si la solicitud puede ser evaluada.
     */
    public boolean puedeSerEvaluada() {
        return estado == EstadoSolicitud.PENDIENTE;
    }

    /**
     * Verifica si la solicitud está aprobada y lista para desembolso.
     */
    public boolean estaAprobada() {
        return estado == EstadoSolicitud.APROBADA;
    }

    /**
     * Genera el número de solicitud no enumerable con SecureRandom.
     */
    public static String generarNumeroSolicitud() {
        int year = java.time.LocalDate.now().getYear();
        int secuencial = new java.security.SecureRandom().nextInt(999999);
        return String.format("SOL-CRED-%d-%06d", year, secuencial);
    }
}
