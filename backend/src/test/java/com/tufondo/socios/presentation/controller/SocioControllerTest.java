package com.tufondo.socios.presentation.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("SocioController - Tests de Seguridad")
class SocioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final UUID SOCIO_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Nested
    @DisplayName("Seguridad - Endpoints requieren autenticación")
    class SeguridadTests {

        @Test
        @DisplayName("GET /api/v1/socios sin auth devuelve 403")
        void listar_sin_auth_devuelve_403() throws Exception {
            mockMvc.perform(get("/api/v1/socios"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/v1/socios/{id} sin auth devuelve 403")
        void obtener_sin_auth_devuelve_403() throws Exception {
            mockMvc.perform(get("/api/v1/socios/{id}", SOCIO_ID))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("PATCH /api/v1/socios/{id}/activar sin auth devuelve 403")
        void activar_sin_auth_devuelve_403() throws Exception {
            mockMvc.perform(patch("/api/v1/socios/{id}/activar", SOCIO_ID))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("PATCH /api/v1/socios/{id}/desactivar sin auth devuelve 403")
        void desactivar_sin_auth_devuelve_403() throws Exception {
            mockMvc.perform(patch("/api/v1/socios/{id}/desactivar", SOCIO_ID))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/v1/socios/buscar sin auth devuelve 403")
        void buscar_sin_auth_devuelve_403() throws Exception {
            mockMvc.perform(get("/api/v1/socios/buscar")
                            .param("nombre", "Juan"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("PUT /api/v1/socios/{id} sin auth devuelve 403")
        void actualizar_sin_auth_devuelve_403() throws Exception {
            mockMvc.perform(put("/api/v1/socios/{id}", SOCIO_ID)
                            .contentType("application/json")
                            .content("{}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("DELETE /api/v1/socios/{id} sin auth devuelve 403")
        void eliminar_sin_auth_devuelve_403() throws Exception {
            mockMvc.perform(delete("/api/v1/socios/{id}", SOCIO_ID))
                    .andExpect(status().isForbidden());
        }
    }
}