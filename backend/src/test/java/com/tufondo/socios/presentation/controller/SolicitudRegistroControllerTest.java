package com.tufondo.socios.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tufondo.socios.application.dto.SolicitudRegistroRequestDTO;
import com.tufondo.socios.application.dto.SolicitudRegistroResponseDTO;
import com.tufondo.socios.application.usecase.AprobarSolicitudUseCase;
import com.tufondo.socios.application.usecase.CrearSolicitudRegistroUseCase;
import com.tufondo.socios.application.usecase.ListarSolicitudesUseCase;
import com.tufondo.socios.application.usecase.RechazarSolicitudUseCase;
import com.tufondo.socios.domain.model.enums.EstadoCivil;
import com.tufondo.socios.domain.model.enums.EstadoSolicitud;
import com.tufondo.socios.domain.model.enums.Genero;
import com.tufondo.socios.domain.model.enums.TipoDocumento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests para POST /api/v1/socios/solicitud.
 *
 * Cubre que el controller hace pasarela de la auditoría LOPDP:
 *  1. Si el BFF envía ipRegistro/userAgentRegistro en el body, se respetan.
 *  2. Si vienen vacíos, se cae a HttpServletRequest (X-Forwarded-For,
 *     User-Agent, remoteAddr) — defensa en profundidad.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("SolicitudRegistroController - POST /solicitud (auditoría LOPDP)")
class SolicitudRegistroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CrearSolicitudRegistroUseCase crearSolicitudUseCase;

    // Mocks adicionales para satisfacer el wiring del controller — no se usan.
    @MockBean
    private ListarSolicitudesUseCase listarSolicitudesUseCase;
    @MockBean
    private AprobarSolicitudUseCase aprobarSolicitudUseCase;
    @MockBean
    private RechazarSolicitudUseCase rechazarSolicitudUseCase;

    private SolicitudRegistroResponseDTO responseStub;

    @BeforeEach
    void setUp() {
        responseStub = SolicitudRegistroResponseDTO.builder()
                .id(UUID.randomUUID())
                .estado(EstadoSolicitud.PENDIENTE)
                .build();
        when(crearSolicitudUseCase.ejecutar(any(SolicitudRegistroRequestDTO.class)))
                .thenReturn(responseStub);
    }

    @Test
    @DisplayName("Cuando el body trae ipRegistro/userAgentRegistro del BFF, se reenvían al use case sin modificación")
    void prefiereValoresDelBffSobreFallbackDelServlet() throws Exception {
        SolicitudRegistroRequestDTO request = buildValidRequest();
        request.setIpRegistro("203.0.113.42");
        request.setUserAgentRegistro("Mozilla/5.0 (Frontend-Captured)");

        mockMvc.perform(post("/api/v1/socios/solicitud")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        // Si el controller cayera al fallback, escogería estos en su lugar.
                        .header("X-Forwarded-For", "1.2.3.4")
                        .header("User-Agent", "TestAgent/1.0"))
                .andExpect(status().isCreated());

        SolicitudRegistroRequestDTO captured = capturarRequest();
        assertThat(captured.getIpRegistro())
                .as("El IP del BFF debe ganar al header del servlet")
                .isEqualTo("203.0.113.42");
        assertThat(captured.getUserAgentRegistro())
                .as("El UA del BFF debe ganar al header del servlet")
                .isEqualTo("Mozilla/5.0 (Frontend-Captured)");
    }

    @Test
    @DisplayName("Sin ipRegistro/userAgentRegistro en body, fallback a X-Forwarded-For y User-Agent")
    void fallbackAlServletCuandoBffNoEnviaMetadata() throws Exception {
        SolicitudRegistroRequestDTO request = buildValidRequest();
        // No seteamos ipRegistro ni userAgentRegistro — simulamos BFF ausente.

        mockMvc.perform(post("/api/v1/socios/solicitud")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-Forwarded-For", "1.2.3.4, 10.0.0.1")
                        .header("User-Agent", "TestAgent/1.0"))
                .andExpect(status().isCreated());

        SolicitudRegistroRequestDTO captured = capturarRequest();
        // Toma el PRIMER hop, no la lista completa ni la IP del proxy.
        assertThat(captured.getIpRegistro())
                .as("Fallback debe tomar el primer hop de X-Forwarded-For")
                .isEqualTo("1.2.3.4");
        assertThat(captured.getUserAgentRegistro())
                .as("Fallback debe tomar el header User-Agent")
                .isEqualTo("TestAgent/1.0");
    }

    @Test
    @DisplayName("Sin ningún header de IP, fallback final a remoteAddr (no null)")
    void fallbackARemoteAddrSiNoHayHeadersDeIp() throws Exception {
        SolicitudRegistroRequestDTO request = buildValidRequest();

        mockMvc.perform(post("/api/v1/socios/solicitud")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        SolicitudRegistroRequestDTO captured = capturarRequest();
        // MockMvc usa "127.0.0.1" como remoteAddr por defecto.
        assertThat(captured.getIpRegistro())
                .as("Sin headers, debe caer a HttpServletRequest.getRemoteAddr()")
                .isNotNull();
    }

    private SolicitudRegistroRequestDTO capturarRequest() {
        ArgumentCaptor<SolicitudRegistroRequestDTO> captor =
                ArgumentCaptor.forClass(SolicitudRegistroRequestDTO.class);
        org.mockito.Mockito.verify(crearSolicitudUseCase).ejecutar(captor.capture());
        return captor.getValue();
    }

    /**
     * Payload válido mínimo según los constraints de
     * {@link SolicitudRegistroRequestDTO}.
     */
    private SolicitudRegistroRequestDTO buildValidRequest() {
        return SolicitudRegistroRequestDTO.builder()
                .nombreCompleto("Juan Pérez García")
                .tipoDocumento(TipoDocumento.CEDULA)
                .cedula("V-12345678")
                .fechaNacimiento(LocalDate.of(1990, 5, 15))
                .genero(Genero.MASCULINO)
                .estadoCivil(EstadoCivil.SOLTERO)
                .correoElectronico("juan@ejemplo.com")
                .telefono("04121234567")
                .empresa("Acme Corp")
                .salario(new BigDecimal("1500.00"))
                .aceptaTerminos(true)
                .aceptaLopdp(true)
                .aceptaLocdoft(true)
                .build();
    }
}
