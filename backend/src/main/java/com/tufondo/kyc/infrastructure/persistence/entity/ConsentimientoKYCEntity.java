// com.tufondo.kyc.infrastructure.persistence.entity.ConsentimientoKYCEntity
package com.tufondo.kyc.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity para ConsentimientoKYC.
 */
@Entity
@Table(name = "consentimiento_kyc",
    indexes = {
        @Index(name = "idx_consentimiento_socio_id", columnList = "socio_id"),
        @Index(name = "idx_consentimiento_fecha", columnList = "fecha_consentimiento")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsentimientoKYCEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "socio_id", nullable = false)
    private UUID socioId;

    @Column(name = "tipo_consentimiento", nullable = false, length = 30)
    private String tipoConsentimiento;

    @Column(name = "aceptado", nullable = false)
    private Boolean aceptado;

    @Column(name = "fecha_consentimiento", nullable = false)
    private LocalDateTime fechaConsentimiento;

    @Column(name = "ip_cliente", nullable = false, length = 45)
    private String ipCliente;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "version_politica", nullable = false, length = 20)
    private String versionPolitica;

    @Version
    @Column(name = "version")
    private Long version;
}