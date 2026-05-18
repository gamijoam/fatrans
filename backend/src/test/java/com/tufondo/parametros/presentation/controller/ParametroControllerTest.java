package com.tufondo.parametros.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tufondo.parametros.application.dto.ActualizarParametroRequest;
import com.tufondo.parametros.application.dto.ParametroResponse;
import com.tufondo.parametros.application.usecase.ParametroService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("ParametroController - Tests de Seguridad")
class ParametroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ParametroService service;

    private static final String KEY = "TASA_INTERES_AHORRO";
    private static final UUID ADMIN_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private ParametroResponse createMockResponse(String key, String valor) {
        return new ParametroResponse(
                key, valor, "PERCENTAGE",
                "Descripción test", "TASA",
                true, Instant.now(), ADMIN_ID
        );
    }

    @Nested
    @DisplayName("Seguridad - Endpoints requieren autenticación")
    class SeguridadTests {

        @Test
        @DisplayName("GET /api/v1/parametros sin auth devuelve 403")
        void listar_sin_auth_devuelve_403() throws Exception {
            mockMvc.perform(get("/api/v1/parametros"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/v1/parametros/{key} sin auth devuelve 403")
        void obtener_sin_auth_devuelve_403() throws Exception {
            mockMvc.perform(get("/api/v1/parametros/{key}", KEY))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/v1/parametros/categoria/{cat} sin auth devuelve 403")
        void listar_por_categoria_sin_auth_devuelve_403() throws Exception {
            mockMvc.perform(get("/api/v1/parametros/categoria/TASA"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("PUT /api/v1/parametros/{key} sin auth devuelve 403")
        void actualizar_sin_auth_devuelve_403() throws Exception {
            mockMvc.perform(put("/api/v1/parametros/{key}", KEY)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"valor\":\"0.06\"}"))
                    .andExpect(status().isForbidden());
        }
    }
}