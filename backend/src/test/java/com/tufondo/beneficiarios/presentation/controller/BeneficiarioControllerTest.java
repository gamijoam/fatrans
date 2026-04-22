// com/tufondo/beneficiarios/presentation/controller/BeneficiarioControllerTest.java
package com.tufondo.beneficiarios.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tufondo.beneficiarios.application.dto.*;
import com.tufondo.beneficiarios.application.port.SocioQueryPort;
import com.tufondo.beneficiarios.application.usecase.*;
import com.tufondo.beneficiarios.domain.model.enums.Parentesco;
import com.tufondo.beneficiarios.domain.model.enums.TipoDocumento;
import com.tufondo.beneficiarios.domain.repository.BeneficiarioRepository;
import com.tufondo.beneficiarios.infrastructure.persistence.adapter.BeneficiarioAuditService;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para BeneficiarioController.
 * Verifica endpoints REST y validación de seguridad IDOR.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("BeneficiarioController - Tests de Integración")
class BeneficiarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BeneficiarioRepository beneficiarioRepository;

    @MockBean
    private SocioQueryPort socioQueryPort;

    @MockBean
    private BeneficiarioAuditService auditService;

    private static final UUID SOCIO_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID BENEFICIARIO_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final String NUMERO_DOCUMENTO_SOCIO = "V-87654321";

    private CreateBeneficiarioRequestDTO createRequest;
    private UpdateBeneficiarioRequestDTO updateRequest;

    @BeforeEach
    void setUp() {
        beneficiarioRepository.listarPorSocioId(SOCIO_ID).forEach(b -> {
            var entity = beneficiarioRepository.buscarPorIdIncluyendoInactivos(b.getId());
            entity.ifPresent(ben -> {
                ben.marcarInactivo();
                beneficiarioRepository.guardar(ben);
            });
        });

        when(socioQueryPort.existsByIdAndActivoTrue(SOCIO_ID)).thenReturn(true);
        when(socioQueryPort.getNumeroDocumentoById(SOCIO_ID)).thenReturn(NUMERO_DOCUMENTO_SOCIO);

        createRequest = new CreateBeneficiarioRequestDTO(
                "Juan Pérez",
                "V-12345678",
                TipoDocumento.CEDULA_IDENTIDAD,
                Parentesco.HIJO,
                new BigDecimal("50.00"),
                "04121234567"
        );

        updateRequest = new UpdateBeneficiarioRequestDTO(
                "Juan Pérez Actualizado",
                "V-12345678",
                TipoDocumento.CEDULA_IDENTIDAD,
                Parentesco.HIJO,
                new BigDecimal("60.00"),
                "04121234567"
        );
    }

    @Nested
    @DisplayName("POST /api/v1/socios/{socioId}/beneficiarios")
    class PostBeneficiarioTests {

        @Test
        @WithMockUser(username = "11111111-1111-1111-1111-111111111111", roles = {"USER"})
        @DisplayName("post_beneficiario_creaYCrea201 - debe crear beneficiario y retornar 201")
        void post_beneficiario_creaYCrea201() throws Exception {
            mockMvc.perform(post("/api/v1/socios/{socioId}/beneficiarios", SOCIO_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.socioId").value(SOCIO_ID.toString()))
                    .andExpect(jsonPath("$.nombreCompleto").value("Juan Pérez"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/socios/{socioId}/beneficiarios")
    class GetBeneficiariosTests {

        @Test
        @WithMockUser(username = "11111111-1111-1111-1111-111111111111", roles = {"USER"})
        @DisplayName("get_beneficiarios_listaCon200 - debe listar beneficiarios y retornar 200")
        void get_beneficiarios_listaCon200() throws Exception {
            mockMvc.perform(get("/api/v1/socios/{socioId}/beneficiarios", SOCIO_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total").isNumber())
                    .andExpect(jsonPath("$.beneficiarios").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/socios/{socioId}/beneficiarios/{id}")
    class GetBeneficiarioPorIdTests {

        @Test
        @WithMockUser(username = "11111111-1111-1111-1111-111111111111", roles = {"USER"})
        @DisplayName("get_beneficiarioPorId_retorna200 - debe obtener beneficiario por ID y retornar 200")
        void get_beneficiarioPorId_retorna200() throws Exception {
            // Crear primero un beneficiario
            var response = mockMvc.perform(post("/api/v1/socios/{socioId}/beneficiarios", SOCIO_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andReturn();

            String beneficiaryId = response.getResponse().getContentAsString()
                    .split("\"id\":\"")[1].split("\"")[0];

            mockMvc.perform(get("/api/v1/socios/{socioId}/beneficiarios/{id}", SOCIO_ID, beneficiaryId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(beneficiaryId));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/socios/{socioId}/beneficiarios/{id}")
    class PutBeneficiarioTests {

        @Test
        @WithMockUser(username = "11111111-1111-1111-1111-111111111111", roles = {"USER"})
        @DisplayName("put_beneficiario_actualizaCon200 - debe actualizar beneficiario y retornar 200")
        void put_beneficiario_actualizaCon200() throws Exception {
            // Crear primero un beneficiario
            var response = mockMvc.perform(post("/api/v1/socios/{socioId}/beneficiarios", SOCIO_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andReturn();

            String beneficiaryId = response.getResponse().getContentAsString()
                    .split("\"id\":\"")[1].split("\"")[0];

            mockMvc.perform(put("/api/v1/socios/{socioId}/beneficiarios/{id}", SOCIO_ID, beneficiaryId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nombreCompleto").value("Juan Pérez Actualizado"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/socios/{socioId}/beneficiarios/{id}")
    class DeleteBeneficiarioTests {

        @Test
        @WithMockUser(username = "11111111-1111-1111-1111-111111111111", roles = {"USER"})
        @DisplayName("delete_beneficiario_softDeleteCon200 - debe hacer soft delete y retornar 200")
        void delete_beneficiario_softDeleteCon200() throws Exception {
            // Crear primero un beneficiario
            var response = mockMvc.perform(post("/api/v1/socios/{socioId}/beneficiarios", SOCIO_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andReturn();

            String beneficiaryId = response.getResponse().getContentAsString()
                    .split("\"id\":\"")[1].split("\"")[0];

            mockMvc.perform(delete("/api/v1/socios/{socioId}/beneficiarios/{id}", SOCIO_ID, beneficiaryId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.activo").value(false));
        }
    }

    @Nested
    @DisplayName("Validaciones de Seguridad IDOR")
    class SeguridadIdorTests {

        @Test
        @WithMockUser(username = "33333333-3333-3333-3333-333333333333", roles = {"USER"})
        @DisplayName("get_beneficiario_idor_devuelve404 - debe retornar 404 cuando usuario intenta acceder a socio ajeno")
        void get_beneficiario_idor_devuelve404() throws Exception {
            UUID otroSocioId = UUID.fromString("33333333-3333-3333-3333-333333333333");

            mockMvc.perform(get("/api/v1/socios/{socioId}/beneficiarios", otroSocioId))
                    .andExpect(status().isNotFound());
        }
    }
}
