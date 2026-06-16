package com.tufondo.contabilidad.application.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties identificatorias de la entidad (sub-issue #269).
 *
 * <p>Razón social, RIF, dirección — datos que aparecen en cabecera de los
 * reportes contables formales (Libro Diario, Libro Mayor, Balance General).
 * Se configuran vía {@code application.yml}:</p>
 *
 * <pre>
 * entidad:
 *   razonSocial: "Asociación Fatrans de Ahorro y Crédito"
 *   rif: "J-XXXXXXX-X"
 *   direccion: "..."
 * </pre>
 *
 * <p><strong>Por qué properties y no parámetros en BD</strong>: los reportes
 * SUDECA exigen cabeceras de identificación fiscal que cambian rara vez
 * (cuando cambia el RIF o la razón social, lo cual implica trámites legales).
 * Properties son configurables sin migration y suficiente para arrancar.
 * Si en el futuro se necesita una UI de admin para editarlos, sub-issue
 * dedicado migra a {@code parametros_sistema}.</p>
 */
@Data
@ConfigurationProperties(prefix = "entidad")
public class EntidadProperties {

    /** Razón social completa que aparece en cabeceras de reportes. */
    private String razonSocial = "Asociación Fatrans de Ahorro y Crédito (configurar)";

    /** RIF venezolano formato J-XXXXXXX-X. */
    private String rif = "J-XXXXXXXX-X (configurar)";

    /** Dirección física, opcional. */
    private String direccion = "";

    /** Sigla corta, opcional. */
    private String sigla = "FATRANS";
}
