package com.tufondo.admin.presentation.controller;

import com.tufondo.admin.application.dto.SesionInfoResponse;
import com.tufondo.admin.application.dto.SesionInvalidationResponse;
import com.tufondo.auth.domain.model.Sesion;
import com.tufondo.auth.domain.model.Usuario;
import com.tufondo.auth.domain.model.enums.Rol;
import com.tufondo.auth.domain.model.enums.TipoToken;
import com.tufondo.auth.domain.repository.SesionRepository;
import com.tufondo.auth.domain.repository.UsuarioRepository;
import com.tufondo.auth.infrastructure.service.EmailService;
import com.tufondo.auth.infrastructure.service.SecurityAuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AdminSesionController - Tests de Integración")
class AdminSesionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SesionRepository sesionRepository;

    @MockBean
    private UsuarioRepository usuarioRepository;

    @MockBean
    private SecurityAuditService auditService;

    @MockBean
    private EmailService emailService;

    private static final UUID USUARIO_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SESION_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID ADMIN_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private Usuario usuarioTest;
    private Sesion sesionTest;

    @BeforeEach
    void setUp() {
        usuarioTest = Usuario.desdeParametros(
                USUARIO_ID,
                "usuario_test",
                "usuario@test.com",
                "hash",
                "Usuario Test",
                Rol.SOCIO,
                UUID.randomUUID(),
                true,
                Instant.now(),
                Instant.now(),
                0,
                null,
                false
        );

        sesionTest = Sesion.desdeParametros(
                SESION_ID,
                USUARIO_ID,
                "refresh_token_hash",
                Instant.now().plusSeconds(900),
                Instant.now().plusSeconds(604800),
                true,
                TipoToken.REFRESH_TOKEN,
                Instant.now(),
                Instant.now()
        );
    }

    @Test
    @DisplayName("GET /api/v1/admin/sesiones/usuario/{id} - Lista sesiones activas")
    @WithMockUser(username = "33333333-3333-3333-3333-333333333333", roles = {"ADMIN"})
    void listar_sesiones_activas_devuelve_lista() throws Exception {
        List<Sesion> sesiones = List.of(sesionTest);
        when(sesionRepository.buscarSesionesActivasPorUsuario(USUARIO_ID)).thenReturn(sesiones);

        mockMvc.perform(get("/api/v1/admin/sesiones/usuario/{usuarioId}", USUARIO_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(SESION_ID.toString()))
                .andExpect(jsonPath("$[0].activa").value(true));

        verify(sesionRepository).buscarSesionesActivasPorUsuario(USUARIO_ID);
    }

    @Test
    @DisplayName("GET /api/v1/admin/sesiones/usuario/{id} - Usuario sin sesiones")
    @WithMockUser(username = "33333333-3333-3333-3333-333333333333", roles = {"ADMIN"})
    void listar_sesiones_usuario_sin_sesiones_devuelve_lista_vacia() throws Exception {
        when(sesionRepository.buscarSesionesActivasPorUsuario(USUARIO_ID)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/admin/sesiones/usuario/{usuarioId}", USUARIO_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("POST /api/v1/admin/sesiones/usuario/{id}/invalidar-todas - Invalida todas las sesiones")
    @WithMockUser(username = "33333333-3333-3333-3333-333333333333", roles = {"ADMIN"})
    void invalidar_todas_las_sesiones_devuelve_respuesta_exitosa() throws Exception {
        List<Sesion> sesiones = List.of(sesionTest);
        when(sesionRepository.buscarSesionesActivasPorUsuario(USUARIO_ID)).thenReturn(sesiones);
        when(usuarioRepository.buscarPorId(USUARIO_ID)).thenReturn(Optional.of(usuarioTest));
        doNothing().when(sesionRepository).invalidarTodasPorUsuario(USUARIO_ID);
        doNothing().when(auditService).logSesionesInvalidadas(any(), any(), any(), anyInt());
        doNothing().when(emailService).enviarNotificacionSesionesInvalidadas(any(), any(), anyInt(), any());

        mockMvc.perform(post("/api/v1/admin/sesiones/usuario/{usuarioId}/invalidar-todas", USUARIO_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value(USUARIO_ID.toString()))
                .andExpect(jsonPath("$.sesionesInvalidadas").value(1))
                .andExpect(jsonPath("$.mensaje").value("Todas las sesiones han sido invalidadas exitosamente"));

        verify(sesionRepository).invalidarTodasPorUsuario(USUARIO_ID);
        verify(auditService).logSesionesInvalidadas(eq(USUARIO_ID), any(), any(), eq(1));
        verify(emailService).enviarNotificacionSesionesInvalidadas(
                eq(usuarioTest.correoElectronico()),
                eq(usuarioTest.nombreUsuario()),
                eq(1),
                any()
        );
    }

    @Test
    @DisplayName("POST /api/v1/admin/sesiones/usuario/{id}/invalidar-todas - Usuario no encontrado")
    @WithMockUser(username = "33333333-3333-3333-3333-333333333333", roles = {"ADMIN"})
    void invalidar_todas_usuario_no_encontrado_envia_email_sin_datos() throws Exception {
        List<Sesion> sesiones = List.of(sesionTest);
        when(sesionRepository.buscarSesionesActivasPorUsuario(USUARIO_ID)).thenReturn(sesiones);
        when(usuarioRepository.buscarPorId(USUARIO_ID)).thenReturn(Optional.empty());
        doNothing().when(sesionRepository).invalidarTodasPorUsuario(USUARIO_ID);
        doNothing().when(auditService).logSesionesInvalidadas(any(), any(), any(), anyInt());

        mockMvc.perform(post("/api/v1/admin/sesiones/usuario/{usuarioId}/invalidar-todas", USUARIO_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sesionesInvalidadas").value(1));

        verify(emailService, never()).enviarNotificacionSesionesInvalidadas(any(), any(), anyInt(), any());
    }

    @Test
    @DisplayName("POST /api/v1/admin/sesiones/{sesionId}/invalidar - Invalida sesión específica")
    @WithMockUser(username = "33333333-3333-3333-3333-333333333333", roles = {"ADMIN"})
    void invalidar_sesion_especifica_devuelve_no_content() throws Exception {
        doNothing().when(sesionRepository).invalidarPorTokenId(SESION_ID.toString());
        doNothing().when(auditService).logSesionIndividualInvalidadas(any(), any(), any(), any());

        mockMvc.perform(post("/api/v1/admin/sesiones/{sesionId}/invalidar", SESION_ID))
                .andExpect(status().isNoContent());

        verify(sesionRepository).invalidarPorTokenId(SESION_ID.toString());
        verify(auditService).logSesionIndividualInvalidadas(eq(null), any(), any(), eq(SESION_ID.toString()));
    }

    @Test
    @DisplayName("POST /api/v1/admin/sesiones/{sesionId}/invalidar - Sesión no existe")
    @WithMockUser(username = "33333333-3333-3333-3333-333333333333", roles = {"ADMIN"})
    void invalidar_sesion_no_existente_devuelve_no_content() throws Exception {
        doNothing().when(sesionRepository).invalidarPorTokenId(any());

        mockMvc.perform(post("/api/v1/admin/sesiones/{sesionId}/invalidar", UUID.randomUUID()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/v1/admin/sesiones/usuario/{id} - Sin autenticación returns 403")
    void listar_sesiones_sin_auth_devuelve_403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/sesiones/usuario/{usuarioId}", USUARIO_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/admin/sesiones/usuario/{id}/invalidar-todas - Usuario sin rol ADMIN")
    @WithMockUser(username = "33333333-3333-3333-3333-333333333333", roles = {"SOCIO"})
    void invalidar_todas_usuario_sin_rol_admin_devuelve_error() throws Exception {
        mockMvc.perform(post("/api/v1/admin/sesiones/usuario/{usuarioId}/invalidar-todas", USUARIO_ID))
                .andExpect(status().is5xxServerError());
    }
}