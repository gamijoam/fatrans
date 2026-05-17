package com.tufondo.socios.infrastructure.persistence.adapter;

import com.tufondo.socios.domain.model.SolicitudRegistro;
import com.tufondo.socios.domain.model.enums.EstadoCivil;
import com.tufondo.socios.domain.model.enums.EstadoSolicitud;
import com.tufondo.socios.domain.model.enums.Genero;
import com.tufondo.socios.domain.model.enums.TipoDocumento;
import com.tufondo.socios.infrastructure.persistence.jpa.SolicitudRegistroJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de integración para el mapping domain <-> entity del repositorio
 * de solicitudes de registro.
 *
 * Cubre el bug G2: el mapper antiguo perdía 15+ campos (incluido el
 * consentimiento LOPDP, exigido por ley en Venezuela).
 */
@DataJpaTest
@AutoConfigureTestDatabase
@ActiveProfiles("test")
@Import(SolicitudRegistroRepositoryImpl.class)
@DisplayName("SolicitudRegistroRepositoryImpl - Mapping completo")
class SolicitudRegistroRepositoryImplTest {

    @Autowired
    private SolicitudRegistroJpaRepository jpaRepository;

    @Autowired
    private SolicitudRegistroRepositoryImpl repository;

    @Test
    @DisplayName("Persiste y recupera TODOS los campos del dominio sin pérdida")
    void persisteYRecuperaTodosLosCampos() {
        // Arrange — dominio completo con todos los campos poblados.
        SolicitudRegistro solicitud = SolicitudRegistro.builder()
                .nombreCompleto("María José González López")
                .tipoDocumento(TipoDocumento.CEDULA)
                .cedula("V-30123456")
                .fechaNacimiento(LocalDate.of(1995, 5, 15))
                .genero(Genero.FEMENINO)
                .estadoCivil(EstadoCivil.SOLTERO)
                .correoElectronico("maria@test.com")
                .telefono("04121234567")
                .empresa("Empresa ABC, C.A.")
                .rifEmpresa("J-12345678-9")
                .departamento("Recursos Humanos")
                .cargo("Analista Senior")
                .salario(new BigDecimal("1500.50"))
                .direccionEstado("Distrito Capital")
                .direccionCiudad("Caracas")
                .direccionMunicipio("Libertador")
                .direccionCalle("Av. Universidad, Edif. Centro Plaza, Piso 5, Apto 5-B")
                .emergenciaNombre("Juan González")
                .emergenciaTelefono("04141234567")
                .emergenciaParentesco("CONYUGE")
                .estado(EstadoSolicitud.PENDIENTE)
                .aceptaTerminos(true)
                .aceptaLopdp(true)
                .aceptaLocdoft(true)
                .fechaSolicitud(LocalDateTime.now())
                .build();

        // Act
        SolicitudRegistro guardada = repository.guardar(solicitud);
        jpaRepository.flush();
        SolicitudRegistro recuperada = repository.buscarPorId(guardada.getId()).orElseThrow();

        // Assert — verificar que ningún campo se perdió.
        assertThat(recuperada.getId()).isNotNull();
        assertThat(recuperada.getNombreCompleto()).isEqualTo("María José González López");
        assertThat(recuperada.getTipoDocumento()).isEqualTo(TipoDocumento.CEDULA);
        assertThat(recuperada.getCedula()).isEqualTo("V-30123456");
        assertThat(recuperada.getFechaNacimiento()).isEqualTo(LocalDate.of(1995, 5, 15));
        assertThat(recuperada.getGenero()).isEqualTo(Genero.FEMENINO);
        assertThat(recuperada.getEstadoCivil()).isEqualTo(EstadoCivil.SOLTERO);
        assertThat(recuperada.getCorreoElectronico()).isEqualTo("maria@test.com");
        assertThat(recuperada.getTelefono()).isEqualTo("04121234567");
        assertThat(recuperada.getEmpresa()).isEqualTo("Empresa ABC, C.A.");
        assertThat(recuperada.getRifEmpresa()).isEqualTo("J-12345678-9");
        assertThat(recuperada.getDepartamento()).isEqualTo("Recursos Humanos");
        assertThat(recuperada.getCargo()).isEqualTo("Analista Senior");
        assertThat(recuperada.getSalario()).isEqualByComparingTo(new BigDecimal("1500.50"));
        assertThat(recuperada.getDireccionEstado()).isEqualTo("Distrito Capital");
        assertThat(recuperada.getDireccionCiudad()).isEqualTo("Caracas");
        assertThat(recuperada.getDireccionMunicipio()).isEqualTo("Libertador");
        assertThat(recuperada.getDireccionCalle()).isEqualTo("Av. Universidad, Edif. Centro Plaza, Piso 5, Apto 5-B");
        assertThat(recuperada.getEmergenciaNombre()).isEqualTo("Juan González");
        assertThat(recuperada.getEmergenciaTelefono()).isEqualTo("04141234567");
        assertThat(recuperada.getEmergenciaParentesco()).isEqualTo("CONYUGE");
        assertThat(recuperada.getEstado()).isEqualTo(EstadoSolicitud.PENDIENTE);
    }

    @Test
    @DisplayName("Preserva aceptaTerminos, aceptaLopdp y aceptaLocdoft (consentimientos legales)")
    void preservaConsentimientosLegales() {
        SolicitudRegistro solicitud = nuevaSolicitudMinima("V-30000001", "lopdp@test.com")
                .aceptaTerminos(true)
                .aceptaLopdp(true)
                .aceptaLocdoft(true)
                .build();

        SolicitudRegistro guardada = repository.guardar(solicitud);
        jpaRepository.flush();
        SolicitudRegistro recuperada = repository.buscarPorId(guardada.getId()).orElseThrow();

        // El mapper antiguo perdía estos dos campos, dejando la columna en NULL
        // (default false al cargar) — bug G2 crítico para auditoría legal.
        assertThat(recuperada.getAceptaTerminos())
                .as("aceptaTerminos debe persistirse; mapping antiguo lo perdía")
                .isTrue();
        assertThat(recuperada.getAceptaLopdp())
                .as("aceptaLopdp debe persistirse; mapping antiguo lo perdía (defecto legal LOPDP)")
                .isTrue();
        assertThat(recuperada.getAceptaLocdoft())
                .as("aceptaLocdoft debe persistirse (#218 PR-B — declaración LOCDOFT)")
                .isTrue();
    }

    @Test
    @DisplayName("Genera el ID automáticamente cuando no se provee")
    void generaIdSiNoSeProvee() {
        SolicitudRegistro solicitud = nuevaSolicitudMinima("V-30000002", "auto-id@test.com")
                .aceptaTerminos(true)
                .aceptaLopdp(true)
                .build();

        SolicitudRegistro guardada = repository.guardar(solicitud);

        assertThat(guardada.getId()).isNotNull();
    }

    @Test
    @DisplayName("Setea fechaSolicitud cuando no se provee")
    void seteaFechaSolicitudCuandoNoSeProvee() {
        SolicitudRegistro solicitud = nuevaSolicitudMinima("V-30000003", "fecha@test.com")
                .aceptaTerminos(true)
                .aceptaLopdp(true)
                .fechaSolicitud(null)
                .build();

        SolicitudRegistro guardada = repository.guardar(solicitud);

        assertThat(guardada.getFechaSolicitud()).isNotNull();
    }

    @Test
    @DisplayName("Persiste los 3 campos LOPDP (ip, user-agent, consent timestamp) sin pérdida")
    void persisteCamposLopdp() {
        // Truncamos a millis porque H2 puede no preservar nanosegundos en TIMESTAMP.
        Instant consentInstant = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) "
                + "Chrome/120.0.0.0 Safari/537.36 TufondoTest/1.0";

        SolicitudRegistro solicitud = nuevaSolicitudMinima("V-30055555", "lopdp-meta@test.com")
                .aceptaTerminos(true)
                .aceptaLopdp(true)
                .ipRegistro("203.0.113.42")
                .userAgentRegistro(userAgent)
                .consentLopdpTimestamp(consentInstant)
                .build();

        SolicitudRegistro guardada = repository.guardar(solicitud);
        jpaRepository.flush();
        SolicitudRegistro recuperada = repository.buscarPorId(guardada.getId()).orElseThrow();

        assertThat(recuperada.getIpRegistro())
                .as("ip_registro debe persistirse para defensa legal LOPDP")
                .isEqualTo("203.0.113.42");
        assertThat(recuperada.getUserAgentRegistro())
                .as("user_agent_registro debe persistirse íntegro hasta 500 chars")
                .isEqualTo(userAgent);
        assertThat(recuperada.getConsentLopdpTimestamp())
                .as("consent_lopdp_timestamp debe persistirse como Instant (ms precision)")
                .isEqualTo(consentInstant);
    }

    @Test
    @DisplayName("Campos LOPDP nullables: si no se proveen, se persisten como NULL")
    void camposLopdpSonNullables() {
        SolicitudRegistro solicitud = nuevaSolicitudMinima("V-30055556", "lopdp-null@test.com")
                .aceptaTerminos(true)
                .aceptaLopdp(true)
                .build();

        SolicitudRegistro guardada = repository.guardar(solicitud);
        jpaRepository.flush();
        SolicitudRegistro recuperada = repository.buscarPorId(guardada.getId()).orElseThrow();

        assertThat(recuperada.getIpRegistro()).isNull();
        assertThat(recuperada.getUserAgentRegistro()).isNull();
        assertThat(recuperada.getConsentLopdpTimestamp()).isNull();
    }

    @Test
    @DisplayName("existePorCedula y existePorCorreo funcionan tras persistir")
    void existenciaPorCedulaYCorreo() {
        SolicitudRegistro solicitud = nuevaSolicitudMinima("V-30000004", "exist@test.com")
                .aceptaTerminos(true)
                .aceptaLopdp(true)
                .build();
        repository.guardar(solicitud);
        jpaRepository.flush();

        assertThat(repository.existePorCedula("V-30000004")).isTrue();
        assertThat(repository.existePorCorreo("exist@test.com")).isTrue();
        assertThat(repository.existePorCedula("V-99999999")).isFalse();
    }

    /**
     * Cubre el bug del dashboard de admin: el card "Solicitudes Pendientes" mostraba 0
     * porque el contador apuntaba a `SolicitudCredito`, no a `SolicitudRegistro`. Este
     * test verifica que la nueva firma `contarPorEstado()` devuelve el conteo correcto
     * filtrado por estado.
     */
    @Test
    @DisplayName("contarPorEstado devuelve el número de solicitudes en ese estado")
    void contarPorEstadoFiltraCorrectamente() {
        // Arrange: 2 pendientes, 1 aprobada.
        repository.guardar(nuevaSolicitudMinima("V-40000001", "p1@test.com")
                .aceptaTerminos(true).aceptaLopdp(true)
                .estado(EstadoSolicitud.PENDIENTE).build());
        repository.guardar(nuevaSolicitudMinima("V-40000002", "p2@test.com")
                .aceptaTerminos(true).aceptaLopdp(true)
                .estado(EstadoSolicitud.PENDIENTE).build());
        repository.guardar(nuevaSolicitudMinima("V-40000003", "a1@test.com")
                .aceptaTerminos(true).aceptaLopdp(true)
                .estado(EstadoSolicitud.APROBADA).build());
        jpaRepository.flush();

        assertThat(repository.contarPorEstado(EstadoSolicitud.PENDIENTE))
                .as("Debe contar las 2 solicitudes pendientes")
                .isEqualTo(2L);
        assertThat(repository.contarPorEstado(EstadoSolicitud.APROBADA))
                .as("Debe contar la única solicitud aprobada")
                .isEqualTo(1L);
        assertThat(repository.contarPorEstado(EstadoSolicitud.RECHAZADA))
                .as("Sin solicitudes rechazadas, debe devolver 0")
                .isEqualTo(0L);
    }

    /**
     * Builder utilitario con los campos mínimos NOT NULL en el schema.
     */
    private SolicitudRegistro.SolicitudRegistroBuilder nuevaSolicitudMinima(String cedula, String correo) {
        return SolicitudRegistro.builder()
                .id(UUID.randomUUID())
                .nombreCompleto("Test User")
                .tipoDocumento(TipoDocumento.CEDULA)
                .cedula(cedula)
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .genero(Genero.MASCULINO)
                .estadoCivil(EstadoCivil.SOLTERO)
                .correoElectronico(correo)
                .telefono("04121234567")
                .empresa("Empresa Test")
                // Consentimientos legales mínimos (#218 PR-B: locdoft también obligatorio)
                .aceptaTerminos(true)
                .aceptaLopdp(true)
                .aceptaLocdoft(true)
                .estado(EstadoSolicitud.PENDIENTE)
                .fechaSolicitud(LocalDateTime.now());
    }
}
