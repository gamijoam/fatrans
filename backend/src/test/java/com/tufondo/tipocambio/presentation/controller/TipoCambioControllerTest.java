package com.tufondo.tipocambio.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tufondo.tipocambio.application.dto.TipoCambioRequest;
import com.tufondo.tipocambio.application.dto.TipoCambioResponse;
import com.tufondo.tipocambio.application.usecase.ConsultarTipoCambioUseCase;
import com.tufondo.tipocambio.application.usecase.GestionarTipoCambioUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("TipoCambioController - Tests de Integración")
class TipoCambioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConsultarTipoCambioUseCase consultarUseCase;

    @MockBean
    private GestionarTipoCambioUseCase gestionarUseCase;

    private static final UUID TIPO_CAMBIO_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ADMIN_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private TipoCambioResponse tasaResponse;
    private List<TipoCambioResponse> historialResponse;

    @BeforeEach
    void setUp() {
        tasaResponse = TipoCambioResponse.builder()
                .id(TIPO_CAMBIO_ID)
                .fecha(LocalDate.of(2026, 4, 26))
                .tasaCompra(new BigDecimal("45.000000"))
                .tasaVenta(new BigDecimal("45.500000"))
                .fuente("BCV")
                .creadoPor(ADMIN_ID)
                .createdAt(Instant.now())
                .variacionPorcentual(new BigDecimal("2.250000"))
                .build();

        TipoCambioResponse tasaAnterior = TipoCambioResponse.builder()
                .id(UUID.randomUUID())
                .fecha(LocalDate.of(2026, 4, 25))
                .tasaCompra(new BigDecimal("44.000000"))
                .tasaVenta(new BigDecimal("44.500000"))
                .fuente("BCV")
                .creadoPor(ADMIN_ID)
                .createdAt(Instant.now().minusSeconds(86400))
                .build();

        historialResponse = Arrays.asList(tasaResponse, tasaAnterior);
    }

    @Nested
    @DisplayName("GET /api/v1/tipos-cambio/actual")
    class ObtenerTasaActualTests {

        @Test
        @DisplayName("Retorna tasa actual con 200")
        @WithMockUser(username = "33333333-3333-3333-3333-333333333333", roles = {"SOCIO"})
        void retorna_tasa_actual() throws Exception {
            when(consultarUseCase.obtenerTasaActual()).thenReturn(Optional.of(tasaResponse));

            mockMvc.perform(get("/api/v1/tipos-cambio/actual"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.tasaVenta").value(45.5))
                    .andExpect(jsonPath("$.fuente").value("BCV"));
        }

        @Test
        @DisplayName("Retorna 404 cuando no hay tasa")
        @WithMockUser(username = "33333333-3333-3333-3333-333333333333", roles = {"SOCIO"})
        void retorna_404_cuando_no_hay_tasa() throws Exception {
            when(consultarUseCase.obtenerTasaActual()).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/v1/tipos-cambio/actual"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/tipos-cambio/fecha/{fecha}")
    class ObtenerPorFechaTests {

        @Test
        @DisplayName("Retorna tasa por fecha")
        @WithMockUser(username = "33333333-3333-3333-3333-333333333333", roles = {"SOCIO"})
        void retorna_tasa_por_fecha() throws Exception {
            when(consultarUseCase.obtenerPorFecha(LocalDate.of(2026, 4, 26)))
                    .thenReturn(Optional.of(tasaResponse));

            mockMvc.perform(get("/api/v1/tipos-cambio/fecha/2026-04-26"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fecha").value("2026-04-26"));
        }

        @Test
        @DisplayName("Retorna 404 para fecha sin tasa")
        @WithMockUser(username = "33333333-3333-3333-3333-333333333333", roles = {"SOCIO"})
        void retorna_404_fecha_sin_tasa() throws Exception {
            when(consultarUseCase.obtenerPorFecha(any())).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/v1/tipos-cambio/fecha/2020-01-01"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/tipos-cambio/historial")
    class ListarHistorialTests {

        @Test
        @DisplayName("Lista historial con límite default")
        @WithMockUser(username = "33333333-3333-3333-3333-333333333333", roles = {"SOCIO"})
        void lista_historial_default() throws Exception {
            when(consultarUseCase.listarHistorial(30)).thenReturn(historialResponse);

            mockMvc.perform(get("/api/v1/tipos-cambio/historial"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        @DisplayName("Lista historial con límite personalizado")
        @WithMockUser(username = "33333333-3333-3333-3333-333333333333", roles = {"SOCIO"})
        void lista_historial_limite_personalizado() throws Exception {
            when(consultarUseCase.listarHistorial(10)).thenReturn(historialResponse);

            mockMvc.perform(get("/api/v1/tipos-cambio/historial")
                            .param("limit", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/tipos-cambio")
    class ListarTodosTests {

        @Test
        @DisplayName("ADMIN puede listar todos")
        @WithMockUser(username = "33333333-3333-3333-3333-333333333333", roles = {"ADMIN"})
        void admin_puede_listar() throws Exception {
            when(consultarUseCase.listarTodos()).thenReturn(historialResponse);

            mockMvc.perform(get("/api/v1/tipos-cambio"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("SOCIO no puede listar todos")
        @WithMockUser(username = "33333333-3333-3333-3333-333333333333", roles = {"SOCIO"})
        void socio_no_puede_listar() throws Exception {
            mockMvc.perform(get("/api/v1/tipos-cambio"))
                    .andExpect(status().is5xxServerError());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/tipos-cambio")
    class CrearTests {

        @Test
        @DisplayName("SUPER_ADMIN puede crear")
        @WithMockUser(username = "33333333-3333-3333-3333-333333333333", roles = {"SUPER_ADMIN"})
        void super_admin_puede_crear() throws Exception {
            TipoCambioRequest request = TipoCambioRequest.builder()
                    .fecha(LocalDate.of(2026, 4, 27))
                    .tasaCompra(new BigDecimal("45.000000"))
                    .tasaVenta(new BigDecimal("45.500000"))
                    .fuente("BCV")
                    .build();

            when(gestionarUseCase.crear(any(), any(), any())).thenReturn(tasaResponse);

            mockMvc.perform(post("/api/v1/tipos-cambio")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("ADMIN no puede crear")
        @WithMockUser(username = "33333333-3333-3333-3333-333333333333", roles = {"ADMIN"})
        void admin_no_puede_crear() throws Exception {
            TipoCambioRequest request = TipoCambioRequest.builder()
                    .fecha(LocalDate.of(2026, 4, 27))
                    .tasaCompra(new BigDecimal("45.000000"))
                    .tasaVenta(new BigDecimal("45.500000"))
                    .build();

            mockMvc.perform(post("/api/v1/tipos-cambio")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is5xxServerError());
        }

        @Test
        @DisplayName("Sin auth devuelve 403")
        void sin_auth_devuelve_403() throws Exception {
            mockMvc.perform(get("/api/v1/tipos-cambio"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/tipos-cambio/{id}")
    class EliminarTests {

        @Test
        @DisplayName("SUPER_ADMIN puede eliminar")
        @WithMockUser(username = "33333333-3333-3333-3333-333333333333", roles = {"SUPER_ADMIN"})
        void super_admin_puede_eliminar() throws Exception {
            mockMvc.perform(delete("/api/v1/tipos-cambio/{id}", TIPO_CAMBIO_ID))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("ADMIN no puede eliminar")
        @WithMockUser(username = "33333333-3333-3333-3333-333333333333", roles = {"ADMIN"})
        void admin_no_puede_eliminar() throws Exception {
            mockMvc.perform(delete("/api/v1/tipos-cambio/{id}", TIPO_CAMBIO_ID))
                    .andExpect(status().is5xxServerError());
        }
    }
}