// com.tufondo.kyc.infrastructure.persistence.entity.AuditKYCEntity
package com.tufondo.kyc.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad de auditoría KYC para cumplimiento regulatorio (LOPDP/SUDEBAN).
 * Registra todos los accesos y operaciones realizadas sobre datos KYC.
 */
@Entity
@Table(name = "audit_kyc",
    indexes = {
        @Index(name = "idx_audit_fecha", columnList = "fecha_evento"),
        @Index(name = "idx_audit_socio_id", columnList = "socio_id"),
        @Index(name = "idx_audit_tipo_evento", columnList = "tipo_evento"),
        @Index(name = "idx_audit_usuario", columnList = "usuario_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditKYCEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Tipos de evento de auditoría.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false, length = 50)
    private TipoEventoAuditoria tipoEvento;

    /**
     * UUID del socio cuyos datos fueron accedidos/modificados.
     */
    @Column(name = "socio_id", nullable = false)
    private UUID socioId;

    /**
     * ID del usuario que realizó la acción (analista, admin, o sistema).
     */
    @Column(name = "usuario_id", nullable = false, length = 100)
    private String usuarioId;

    /**
     * Rol del usuario en el momento de la acción.
     */
    @Column(name = "rol_usuario", length = 30)
    private String rolUsuario;

    /**
     * Endpoint HTTP que fue invocado.
     */
    @Column(name = "endpoint", length = 500)
    private String endpoint;

    /**
     * Método HTTP (GET, POST, PUT, DELETE).
     */
    @Column(name = "metodo_http", length = 10)
    private String metodoHttp;

    /**
     * Dirección IP del cliente.
     */
    @Column(name = "ip_cliente", length = 45)
    private String ipCliente;

    /**
     * User-Agent del cliente.
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * Identificador de la verificación KYC afectada (si aplica).
     */
    @Column(name = "verificacion_id")
    private UUID verificacionId;

    /**
     * Identificador del documento afectado (si aplica).
     */
    @Column(name = "documento_id")
    private UUID documentoId;

    /**
     * Descripción detallada del evento.
     */
    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    /**
     * Datos adicionales en formato JSON (para detalles de cambio de estado, etc).
     */
    @Column(name = "datos_adicionales", columnDefinition = "TEXT")
    private String datosAdicionales;

    /**
     * Estado anterior (para transiciones de estado).
     */
    @Column(name = "estado_anterior", length = 20)
    private String estadoAnterior;

    /**
     * Nuevo estado (para transiciones de estado).
     */
    @Column(name = "estado_nuevo", length = 20)
    private String estadoNuevo;

    /**
     * Si el acceso fue exitoso o rechazado.
     */
    @Column(name = "exitoso", nullable = false)
    private Boolean exitoso;

    /**
     * Código de error si la operación falló.
     */
    @Column(name = "codigo_error", length = 20)
    private String codigoError;

    /**
     * Timestamp del evento.
     */
    @Column(name = "fecha_evento", nullable = false)
    private LocalDateTime fechaEvento;

    @PrePersist
    protected void onCreate() {
        if (fechaEvento == null) {
            fechaEvento = LocalDateTime.now();
        }
    }

    /**
     * Tipos de eventos de auditoría KYC.
     */
    public enum TipoEventoAuditoria {
        // Acceso a datos
        ACCESO_VERIFICACION,
        ACCESO_DOCUMENTO,
        CONSULTA_ESTADO_KYC,

        // Modificaciones de verificación
        VERIFICACION_ENVIADA,
        VERIFICACION_ACEPTADA,
        VERIFICACION_RECHAZADA,
        VERIFICACION_REENVIADA,
        SOLICITUD_INFO_ADICIONAL,

        // Modificaciones de documento
        DOCUMENTO_SUBIDO,
        DOCUMENTO_ELIMINADO,
        DOCUMENTO_VALIDADO,
        DOCUMENTO_RECHAZADO,

        // Consentimiento
        CONSENTIMIENTO_CREADO,
        CONSENTIMIENTO_REVOCADO,

        // Intentos de acceso no autorizado
        ACCESO_DENEGADO_IDOR,
        ACCESO_DENEGADO_ESTADO,
        ACCESO_DENEGADO_ROL,

        // Rate limiting
        RATE_LIMIT_EXCEDIDO,

        // Errores
        ERROR_OPERACION
    }
}