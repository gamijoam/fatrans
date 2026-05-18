package com.tufondo.socios.application.usecase;

import com.tufondo.socios.application.dto.SolicitudRegistroResponseDTO;
import com.tufondo.socios.domain.model.SolicitudRegistro;
import com.tufondo.socios.domain.model.enums.EstadoCivil;
import com.tufondo.socios.domain.model.enums.EstadoSolicitud;
import com.tufondo.socios.domain.model.enums.Genero;
import com.tufondo.socios.domain.model.enums.TipoDocumento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Cubre el bug del panel de admin: el DTO de respuesta antes solo exponía
 * 12 campos básicos y dejaba al admin a ciegas sobre los datos completos
 * (dirección, datos laborales, contacto de emergencia, consentimientos).
 * Este test verifica que TODOS los campos relevantes se propagan al DTO.
 */
@DisplayName("SolicitudRegistroDTOMapper - mapeo completo")
class SolicitudRegistroDTOMapperTest {

    private final SolicitudRegistroDTOMapper mapper = new SolicitudRegistroDTOMapper();

    @Test
    @DisplayName("Mapea todos los campos del dominio al DTO de respuesta")
    void mapeoCompleto() {
        UUID id = UUID.randomUUID();
        LocalDate nacimiento = LocalDate.of(1990, 5, 15);
        LocalDateTime fechaSolicitud = LocalDateTime.of(2026, 5, 15, 10, 0);
        LocalDateTime fechaRevision = LocalDateTime.of(2026, 5, 16, 9, 30);

        SolicitudRegistro solicitud = SolicitudRegistro.builder()
                .id(id)
                .nombreCompleto("María José González López")
                .tipoDocumento(TipoDocumento.CEDULA)
                .cedula("V-12345678")
                .fechaNacimiento(nacimiento)
                .genero(Genero.FEMENINO)
                .estadoCivil(EstadoCivil.CASADO)
                .correoElectronico("maria@ejemplo.com")
                .telefono("04121234567")
                .empresa("Acme Corp")
                .rifEmpresa("J-12345678-9")
                .departamento("Sistemas")
                .cargo("Analista")
                .salario(new BigDecimal("1500.00"))
                .direccionEstado("Distrito Capital")
                .direccionCiudad("Caracas")
                .direccionMunicipio("Libertador")
                .direccionCalle("Av. Principal #123")
                .emergenciaNombre("Juan González")
                .emergenciaTelefono("04141234567")
                .emergenciaParentesco("Cónyuge")
                .aceptaTerminos(true)
                .aceptaLopdp(true)
                .aceptaLocdoft(true)
                .estado(EstadoSolicitud.APROBADA)
                .fechaSolicitud(fechaSolicitud)
                .fechaRevision(fechaRevision)
                .revisadoPor("admin-uuid")
                .comentario("Todo correcto")
                .motivoRechazo(null)
                .build();

        SolicitudRegistroResponseDTO dto = mapper.toResponseDTO(solicitud);

        // Identificación
        assertThat(dto.getId()).isEqualTo(id);
        // Datos personales (los nuevos en el DTO — antes no se exponían)
        assertThat(dto.getNombreCompleto()).isEqualTo("María José González López");
        assertThat(dto.getTipoDocumento()).isEqualTo(TipoDocumento.CEDULA);
        assertThat(dto.getCedula()).isEqualTo("V-12345678");
        assertThat(dto.getFechaNacimiento()).isEqualTo(nacimiento);
        assertThat(dto.getGenero()).isEqualTo(Genero.FEMENINO);
        assertThat(dto.getEstadoCivil()).isEqualTo(EstadoCivil.CASADO);
        // Contacto
        assertThat(dto.getCorreoElectronico()).isEqualTo("maria@ejemplo.com");
        assertThat(dto.getTelefono()).isEqualTo("04121234567");
        // Laboral
        assertThat(dto.getEmpresa()).isEqualTo("Acme Corp");
        assertThat(dto.getRifEmpresa()).isEqualTo("J-12345678-9");
        assertThat(dto.getDepartamento()).isEqualTo("Sistemas");
        assertThat(dto.getCargo()).isEqualTo("Analista");
        assertThat(dto.getSalario()).isEqualByComparingTo("1500.00");
        // Dirección
        assertThat(dto.getDireccionEstado()).isEqualTo("Distrito Capital");
        assertThat(dto.getDireccionCiudad()).isEqualTo("Caracas");
        assertThat(dto.getDireccionMunicipio()).isEqualTo("Libertador");
        assertThat(dto.getDireccionCalle()).isEqualTo("Av. Principal #123");
        // Emergencia
        assertThat(dto.getEmergenciaNombre()).isEqualTo("Juan González");
        assertThat(dto.getEmergenciaTelefono()).isEqualTo("04141234567");
        assertThat(dto.getEmergenciaParentesco()).isEqualTo("Cónyuge");
        // Consentimientos
        assertThat(dto.getAceptaTerminos()).isTrue();
        assertThat(dto.getAceptaLopdp()).isTrue();
        // Trazabilidad
        assertThat(dto.getEstado()).isEqualTo(EstadoSolicitud.APROBADA);
        assertThat(dto.getFechaSolicitud()).isEqualTo(fechaSolicitud);
        assertThat(dto.getFechaRevision()).isEqualTo(fechaRevision);
        assertThat(dto.getRevisadoPor()).isEqualTo("admin-uuid");
        assertThat(dto.getComentario()).isEqualTo("Todo correcto");
        assertThat(dto.getMotivoRechazo()).isNull();
    }

    @Test
    @DisplayName("Campos opcionales del dominio se mapean como null (no excepción)")
    void mapeoCamposOpcionalesNulos() {
        SolicitudRegistro solicitud = SolicitudRegistro.builder()
                .id(UUID.randomUUID())
                .nombreCompleto("Juan Pérez")
                .tipoDocumento(TipoDocumento.CEDULA)
                .cedula("V-87654321")
                .fechaNacimiento(LocalDate.of(2000, 1, 1))
                .genero(Genero.MASCULINO)
                .estadoCivil(EstadoCivil.SOLTERO)
                .correoElectronico("juan@test.com")
                .telefono("04121111111")
                .empresa("Empresa")
                .estado(EstadoSolicitud.PENDIENTE)
                .fechaSolicitud(LocalDateTime.now())
                // Todos los opcionales quedan null intencionalmente.
                .build();

        SolicitudRegistroResponseDTO dto = mapper.toResponseDTO(solicitud);

        assertThat(dto.getRifEmpresa()).isNull();
        assertThat(dto.getDepartamento()).isNull();
        assertThat(dto.getCargo()).isNull();
        assertThat(dto.getSalario()).isNull();
        assertThat(dto.getDireccionEstado()).isNull();
        assertThat(dto.getDireccionCiudad()).isNull();
        assertThat(dto.getDireccionMunicipio()).isNull();
        assertThat(dto.getDireccionCalle()).isNull();
        assertThat(dto.getEmergenciaNombre()).isNull();
        assertThat(dto.getEmergenciaTelefono()).isNull();
        assertThat(dto.getEmergenciaParentesco()).isNull();
        // Estado debe seguir reflejándose aunque no haya pasado por revisión.
        assertThat(dto.getEstado()).isEqualTo(EstadoSolicitud.PENDIENTE);
        assertThat(dto.getFechaRevision()).isNull();
        assertThat(dto.getRevisadoPor()).isNull();
    }
}
