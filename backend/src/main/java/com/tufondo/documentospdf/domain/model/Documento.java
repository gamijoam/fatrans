// com.tufondo.documentospdf.domain.model.Documento
package com.tufondo.documentospdf.domain.model;

import com.tufondo.documentospdf.domain.model.enums.ClasificacionDocumento;
import com.tufondo.documentospdf.domain.model.enums.EstadoDocumento;
import com.tufondo.documentospdf.domain.model.enums.TipoDocumento;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad de dominio que representa un documento PDF generado.
 * Es una clase inmutable que garantiza la integridad del documento.
 */
public final class Documento {

    private final UUID id;
    private final UUID socioId;
    private final TipoDocumento tipo;
    private final EstadoDocumento estado;
    private final String nombreArchivo;
    private final String rutaAlmacenamiento;
    private final String hashArchivo;
    private final String firmaDigital;
    private final Long tamanoBytes;
    private final LocalDateTime fechaGeneracion;
    private final LocalDateTime fechaExpiracion;
    private final String generadoPor;
    private final ClasificacionDocumento clasificacion;

    private Documento(Builder builder) {
        this.id = builder.id;
        this.socioId = builder.socioId;
        this.tipo = builder.tipo;
        this.estado = builder.estado;
        this.nombreArchivo = builder.nombreArchivo;
        this.rutaAlmacenamiento = builder.rutaAlmacenamiento;
        this.hashArchivo = builder.hashArchivo;
        this.firmaDigital = builder.firmaDigital;
        this.tamanoBytes = builder.tamanoBytes;
        this.fechaGeneracion = builder.fechaGeneracion;
        this.fechaExpiracion = builder.fechaExpiracion;
        this.generadoPor = builder.generadoPor;
        this.clasificacion = builder.clasificacion;
    }

    // Métodos de fábrica
    public static Documento crear(UUID socioId, TipoDocumento tipo, String nombreArchivo,
                                  String rutaAlmacenamiento, String hashArchivo,
                                  Long tamanoBytes, String generadoPor,
                                  ClasificacionDocumento clasificacion) {
        return builder()
                .id(UUID.randomUUID())
                .socioId(socioId)
                .tipo(tipo)
                .estado(EstadoDocumento.GENERADO)
                .nombreArchivo(nombreArchivo)
                .rutaAlmacenamiento(rutaAlmacenamiento)
                .hashArchivo(hashArchivo)
                .tamanoBytes(tamanoBytes)
                .fechaGeneracion(LocalDateTime.now())
                .fechaExpiracion(calcularFechaExpiracion(tipo))
                .generadoPor(generadoPor)
                .clasificacion(clasificacion)
                .build();
    }

    private static LocalDateTime calcularFechaExpiracion(TipoDocumento tipo) {
        // Contratos y pagarés no expiran
        if (tipo == TipoDocumento.CONTRATO_ADHESION || tipo == TipoDocumento.PAGARE) {
            return null;
        }
        // Estados de cuenta expiran en 7 días
        if (tipo == TipoDocumento.ESTADO_CUENTA) {
            return LocalDateTime.now().plusDays(7);
        }
        // Otros documentos expiran en 30 días
        return LocalDateTime.now().plusDays(30);
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getSocioId() { return socioId; }
    public TipoDocumento getTipo() { return tipo; }
    public EstadoDocumento getEstado() { return estado; }
    public String getNombreArchivo() { return nombreArchivo; }
    public String getRutaAlmacenamiento() { return rutaAlmacenamiento; }
    public String getHashArchivo() { return hashArchivo; }
    public String getFirmaDigital() { return firmaDigital; }
    public Long getTamanoBytes() { return tamanoBytes; }
    public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
    public String getGeneradoPor() { return generadoPor; }
    public ClasificacionDocumento getClasificacion() { return clasificacion; }

    // Métodos de transición de estado
    public Documento marcarComoAlmacenado() {
        return builder()
                .id(this.id)
                .socioId(this.socioId)
                .tipo(this.tipo)
                .estado(EstadoDocumento.ALMACENADO)
                .nombreArchivo(this.nombreArchivo)
                .rutaAlmacenamiento(this.rutaAlmacenamiento)
                .hashArchivo(this.hashArchivo)
                .firmaDigital(this.firmaDigital)
                .tamanoBytes(this.tamanoBytes)
                .fechaGeneracion(this.fechaGeneracion)
                .fechaExpiracion(this.fechaExpiracion)
                .generadoPor(this.generadoPor)
                .clasificacion(this.clasificacion)
                .build();
    }

    public Documento conFirmaDigital(String firmaDigital) {
        return builder()
                .id(this.id)
                .socioId(this.socioId)
                .tipo(this.tipo)
                .estado(this.estado)
                .nombreArchivo(this.nombreArchivo)
                .rutaAlmacenamiento(this.rutaAlmacenamiento)
                .hashArchivo(this.hashArchivo)
                .firmaDigital(firmaDigital)
                .tamanoBytes(this.tamanoBytes)
                .fechaGeneracion(this.fechaGeneracion)
                .fechaExpiracion(this.fechaExpiracion)
                .generadoPor(this.generadoPor)
                .clasificacion(this.clasificacion)
                .build();
    }

    public boolean esDescargable() {
        return this.estado.esDescargable();
    }

    public boolean requiereFirmaDigital() {
        return this.tipo.requiereFirmaDigital();
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID socioId;
        private TipoDocumento tipo;
        private EstadoDocumento estado;
        private String nombreArchivo;
        private String rutaAlmacenamiento;
        private String hashArchivo;
        private String firmaDigital;
        private Long tamanoBytes;
        private LocalDateTime fechaGeneracion;
        private LocalDateTime fechaExpiracion;
        private String generadoPor;
        private ClasificacionDocumento clasificacion;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder socioId(UUID socioId) { this.socioId = socioId; return this; }
        public Builder tipo(TipoDocumento tipo) { this.tipo = tipo; return this; }
        public Builder estado(EstadoDocumento estado) { this.estado = estado; return this; }
        public Builder nombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; return this; }
        public Builder rutaAlmacenamiento(String rutaAlmacenamiento) { this.rutaAlmacenamiento = rutaAlmacenamiento; return this; }
        public Builder hashArchivo(String hashArchivo) { this.hashArchivo = hashArchivo; return this; }
        public Builder firmaDigital(String firmaDigital) { this.firmaDigital = firmaDigital; return this; }
        public Builder tamanoBytes(Long tamanoBytes) { this.tamanoBytes = tamanoBytes; return this; }
        public Builder fechaGeneracion(LocalDateTime fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; return this; }
        public Builder fechaExpiracion(LocalDateTime fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; return this; }
        public Builder generadoPor(String generadoPor) { this.generadoPor = generadoPor; return this; }
        public Builder clasificacion(ClasificacionDocumento clasificacion) { this.clasificacion = clasificacion; return this; }

        public Documento build() {
            return new Documento(this);
        }
    }
}
