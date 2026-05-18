package com.tufondo.kyc.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "biometric_consent")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsentimientoBiometricoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "socio_id", nullable = false)
    private UUID socioId;

    @Column(name = "version_politica", nullable = false, length = 20)
    private String versionPolitica;

    @Column(name = "proveedor_destino", nullable = false, length = 50)
    private String proveedorDestino;

    @Column(name = "pais_procesamiento", nullable = false, length = 50)
    private String paisProcesamiento;

    @Column(name = "aceptado", nullable = false)
    private Boolean aceptado;

    @Column(name = "fecha_consentimiento", nullable = false)
    private LocalDateTime fechaConsentimiento;

    @Column(name = "fecha_revocacion")
    private LocalDateTime fechaRevocacion;

    @Column(name = "ip_cliente", length = 45)
    private String ipCliente;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
