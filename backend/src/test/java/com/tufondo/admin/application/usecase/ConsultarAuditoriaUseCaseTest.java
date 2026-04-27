package com.tufondo.admin.application.usecase;

import com.tufondo.admin.application.dto.AuditLogResponse;
import com.tufondo.auth.domain.model.Usuario;
import com.tufondo.auth.domain.model.enums.Rol;
import com.tufondo.auth.domain.repository.UsuarioRepository;
import com.tufondo.auth.infrastructure.persistence.entity.SecurityAuditEntity;
import com.tufondo.auth.infrastructure.persistence.jpa.SecurityAuditJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConsultarAuditoriaUseCase - Tests Completos")
class ConsultarAuditoriaUseCaseTest {

    @Mock
    private SecurityAuditJpaRepository auditRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    private ConsultarAuditoriaUseCase useCase;

    private UUID usuarioId;
    private UUID adminId;
    private SecurityAuditEntity auditLogin;
    private SecurityAuditEntity auditLogout;
    private SecurityAuditEntity auditTipoCreditoCreado;
    private SecurityAuditEntity auditAdminCreado;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        useCase = new ConsultarAuditoriaUseCase(auditRepository, usuarioRepository);

        usuarioId = UUID.randomUUID();
        adminId = UUID.randomUUID();

        usuario = Usuario.desdeParametros(
                usuarioId,
                "admin_test",
                "admin@test.com",
                "hash",
                "Admin Test",
                Rol.ADMIN,
                null,
                true,
                Instant.now(),
                Instant.now(),
                0,
                null,
                false
        );

        auditLogin = SecurityAuditEntity.builder()
                .id(UUID.randomUUID())
                .tipoEvento("LOGIN_SUCCESS")
                .usuarioId(usuarioId)
                .ipAddress("192.168.1.1")
                .timestamp(Instant.now().minus(1, ChronoUnit.HOURS))
                .detalles("Login exitoso")
                .build();

        auditLogout = SecurityAuditEntity.builder()
                .id(UUID.randomUUID())
                .tipoEvento("LOGOUT")
                .usuarioId(usuarioId)
                .ipAddress("192.168.1.1")
                .timestamp(Instant.now().minus(30, ChronoUnit.MINUTES))
                .detalles("Logout exitoso")
                .build();

        auditTipoCreditoCreado = SecurityAuditEntity.builder()
                .id(UUID.randomUUID())
                .tipoEvento("TIPO_CREDITO_CREADO")
                .usuarioId(adminId)
                .ipAddress("192.168.1.100")
                .timestamp(Instant.now().minus(2, ChronoUnit.HOURS))
                .detalles("Tipo crédito MICRO_CREDITO creado")
                .entityType("TIPO_CREDITO")
                .entityId(UUID.randomUUID().toString())
                .action("TIPO_CREDITO_CREADO")
                .build();

        auditAdminCreado = SecurityAuditEntity.builder()
                .id(UUID.randomUUID())
                .tipoEvento("ADMIN_CREADO")
                .usuarioId(adminId)
                .ipAddress("192.168.1.100")
                .timestamp(Instant.now().minus(3, ChronoUnit.HOURS))
                .detalles("Admin nuevo_admin creado")
                .entityType("USUARIO")
                .entityId(UUID.randomUUID().toString())
                .action("ADMIN_CREADO")
                .build();
    }

    @Nested
    @DisplayName("listarAuditoria")
    class ListarAuditoriaTests {

        @Test
        @DisplayName("Lista auditoría con paginación por defecto")
        void lista_auditoria_paginacion_default() {
            List<SecurityAuditEntity> audits = List.of(auditLogin, auditLogout);
            Page<SecurityAuditEntity> page = new PageImpl<>(audits, PageRequest.of(0, 20), 2);

            when(auditRepository.buscarConFiltros(any(), any(), any(), any(), any())).thenReturn(page);
            when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));

            Map<String, Object> result = useCase.listarAuditoria(0, 20, null, null, null, null);

            assertThat(result).containsKeys("auditoria", "page", "size", "totalElements", "totalPages", "first", "last");
            assertThat(result.get("totalElements")).isEqualTo(2L);
            assertThat(result.get("page")).isEqualTo(0);
            assertThat(result.get("totalPages")).isEqualTo(1);

            @SuppressWarnings("unchecked")
            List<AuditLogResponse> auditoria = (List<AuditLogResponse>) result.get("auditoria");
            assertThat(auditoria).hasSize(2);
        }

        @Test
        @DisplayName("Lista auditoría filtrada por usuarioId")
        void lista_auditoria_filtrada_por_usuario() {
            List<SecurityAuditEntity> audits = List.of(auditLogin, auditLogout);
            Page<SecurityAuditEntity> page = new PageImpl<>(audits, PageRequest.of(0, 20), 2);

            when(auditRepository.buscarConFiltros(eq(usuarioId), any(), any(), any(), any())).thenReturn(page);
            when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));

            Map<String, Object> result = useCase.listarAuditoria(0, 20, usuarioId, null, null, null);

            assertThat(result.get("totalElements")).isEqualTo(2L);
            verify(auditRepository).buscarConFiltros(eq(usuarioId), isNull(), isNull(), isNull(), any());
        }

        @Test
        @DisplayName("Lista auditoría filtrada por tipoEvento")
        void lista_auditoria_filtrada_por_tipo() {
            List<SecurityAuditEntity> audits = List.of(auditLogin);
            Page<SecurityAuditEntity> page = new PageImpl<>(audits, PageRequest.of(0, 20), 1);

            when(auditRepository.buscarConFiltros(any(), eq("LOGIN_SUCCESS"), any(), any(), any())).thenReturn(page);
            when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));

            Map<String, Object> result = useCase.listarAuditoria(0, 20, null, "LOGIN_SUCCESS", null, null);

            assertThat(result.get("totalElements")).isEqualTo(1L);
            verify(auditRepository).buscarConFiltros(isNull(), eq("LOGIN_SUCCESS"), isNull(), isNull(), any());
        }

        @Test
        @DisplayName("Lista auditoría filtrada por rango de fechas")
        void lista_auditoria_filtrada_por_fechas() {
            Instant fechaInicio = Instant.now().minus(1, ChronoUnit.DAYS);
            Instant fechaFin = Instant.now();
            List<SecurityAuditEntity> audits = List.of(auditLogin, auditLogout);
            Page<SecurityAuditEntity> page = new PageImpl<>(audits, PageRequest.of(0, 20), 2);

            when(auditRepository.buscarConFiltros(any(), any(), eq(fechaInicio), eq(fechaFin), any())).thenReturn(page);
            when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));

            Map<String, Object> result = useCase.listarAuditoria(0, 20, null, null, fechaInicio, fechaFin);

            assertThat(result.get("totalElements")).isEqualTo(2L);
            verify(auditRepository).buscarConFiltros(isNull(), isNull(), eq(fechaInicio), eq(fechaFin), any());
        }

        @Test
        @DisplayName("Lista auditoría con múltiples filtros combinados")
        void lista_auditoria_filtros_combinados() {
            List<SecurityAuditEntity> audits = List.of(auditLogin);
            Page<SecurityAuditEntity> page = new PageImpl<>(audits, PageRequest.of(0, 20), 1);

            when(auditRepository.buscarConFiltros(eq(usuarioId), eq("LOGIN_SUCCESS"), any(), any(), any())).thenReturn(page);
            when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));

            Instant fechaInicio = Instant.now().minus(1, ChronoUnit.DAYS);
            Instant fechaFin = Instant.now();

            Map<String, Object> result = useCase.listarAuditoria(0, 20, usuarioId, "LOGIN_SUCCESS", fechaInicio, fechaFin);

            assertThat(result.get("totalElements")).isEqualTo(1L);
            verify(auditRepository).buscarConFiltros(eq(usuarioId), eq("LOGIN_SUCCESS"), eq(fechaInicio), eq(fechaFin), any());
        }

        @Test
        @DisplayName("Lista auditoría vacía cuando no hay resultados")
        void lista_auditoria_vacia() {
            Page<SecurityAuditEntity> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);

            when(auditRepository.buscarConFiltros(any(), any(), any(), any(), any())).thenReturn(emptyPage);

            Map<String, Object> result = useCase.listarAuditoria(0, 20, null, null, null, null);

            assertThat(result.get("totalElements")).isEqualTo(0L);
            @SuppressWarnings("unchecked")
            List<AuditLogResponse> auditoria = (List<AuditLogResponse>) result.get("auditoria");
            assertThat(auditoria).isEmpty();
        }

        @Test
        @DisplayName("Incluye nombre de usuario en respuesta")
        void incluye_nombre_usuario_en_respuesta() {
            List<SecurityAuditEntity> audits = List.of(auditLogin);
            Page<SecurityAuditEntity> page = new PageImpl<>(audits, PageRequest.of(0, 20), 1);

            when(auditRepository.buscarConFiltros(any(), any(), any(), any(), any())).thenReturn(page);
            when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));

            Map<String, Object> result = useCase.listarAuditoria(0, 20, null, null, null, null);

            @SuppressWarnings("unchecked")
            List<AuditLogResponse> auditoria = (List<AuditLogResponse>) result.get("auditoria");
            assertThat(auditoria.get(0).getNombreUsuario()).isEqualTo("Admin Test");
        }

        @Test
        @DisplayName("Maneja usuario no encontrado en base de datos")
        void maneja_usuario_no_encontrado() {
            List<SecurityAuditEntity> audits = List.of(auditLogin);
            Page<SecurityAuditEntity> page = new PageImpl<>(audits, PageRequest.of(0, 20), 1);

            when(auditRepository.buscarConFiltros(any(), any(), any(), any(), any())).thenReturn(page);
            when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.empty());

            Map<String, Object> result = useCase.listarAuditoria(0, 20, null, null, null, null);

            @SuppressWarnings("unchecked")
            List<AuditLogResponse> auditoria = (List<AuditLogResponse>) result.get("auditoria");
            assertThat(auditoria.get(0).getNombreUsuario()).isEqualTo("Usuario no encontrado");
        }

        @Test
        @DisplayName("Incluye información de entity para eventos de tipo entidad")
        void incluye_info_entity() {
            List<SecurityAuditEntity> audits = List.of(auditTipoCreditoCreado);
            Page<SecurityAuditEntity> page = new PageImpl<>(audits, PageRequest.of(0, 20), 1);

            when(auditRepository.buscarConFiltros(any(), any(), any(), any(), any())).thenReturn(page);
            when(usuarioRepository.buscarPorId(adminId)).thenReturn(Optional.of(usuario));

            Map<String, Object> result = useCase.listarAuditoria(0, 20, null, null, null, null);

            @SuppressWarnings("unchecked")
            List<AuditLogResponse> auditoria = (List<AuditLogResponse>) result.get("auditoria");
            assertThat(auditoria.get(0).getEntityType()).isEqualTo("TIPO_CREDITO");
            assertThat(auditoria.get(0).getAction()).isEqualTo("TIPO_CREDITO_CREADO");
        }

        @Test
        @DisplayName("Paginación segunda página")
        void paginacion_segunda_pagina() {
            List<SecurityAuditEntity> audits = List.of(auditLogout);
            Page<SecurityAuditEntity> page = new PageImpl<>(audits, PageRequest.of(1, 1), 2);

            when(auditRepository.buscarConFiltros(any(), any(), any(), any(), any())).thenReturn(page);
            when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));

            Map<String, Object> result = useCase.listarAuditoria(1, 1, null, null, null, null);

            assertThat(result.get("page")).isEqualTo(1);
            assertThat(result.get("first")).isEqualTo(false);
            assertThat(result.get("last")).isEqualTo(true);
        }
    }

    @Nested
    @DisplayName("listarRecientes")
    class ListarRecientesTests {

        @Test
        @DisplayName("Lista últimos 100 eventos ordenados por timestamp descendente")
        void lista_recientes_100() {
            List<SecurityAuditEntity> audits = List.of(auditLogin, auditLogout, auditTipoCreditoCreado, auditAdminCreado);
            when(auditRepository.findTop100ByOrderByTimestampDesc()).thenReturn(audits);
            when(usuarioRepository.buscarPorId(any())).thenReturn(Optional.of(usuario));

            List<AuditLogResponse> result = useCase.listarRecientes(100);

            assertThat(result).hasSize(4);
            verify(auditRepository).findTop100ByOrderByTimestampDesc();
        }

        @Test
        @DisplayName("Limita resultados cuando limit es menor que 100")
        void limita_resultados() {
            List<SecurityAuditEntity> audits = List.of(auditLogin, auditLogout, auditTipoCreditoCreado);
            when(auditRepository.findTop100ByOrderByTimestampDesc()).thenReturn(audits);
            when(usuarioRepository.buscarPorId(any())).thenReturn(Optional.of(usuario));

            List<AuditLogResponse> result = useCase.listarRecientes(2);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Retorna lista vacía cuando no hay eventos")
        void retorna_vacio_cuando_no_hay_eventos() {
            when(auditRepository.findTop100ByOrderByTimestampDesc()).thenReturn(List.of());

            List<AuditLogResponse> result = useCase.listarRecientes(50);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Limit 0 retorna todos los resultados")
        void limit_0_retorna_todos() {
            List<SecurityAuditEntity> audits = List.of(auditLogin, auditLogout);
            when(auditRepository.findTop100ByOrderByTimestampDesc()).thenReturn(audits);
            when(usuarioRepository.buscarPorId(any())).thenReturn(Optional.of(usuario));

            List<AuditLogResponse> result = useCase.listarRecientes(0);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Incluye nombre de usuario para cada evento")
        void incluye_nombre_usuario_cada_evento() {
            List<SecurityAuditEntity> audits = List.of(auditLogin, auditLogout);
            when(auditRepository.findTop100ByOrderByTimestampDesc()).thenReturn(audits);
            when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));

            List<AuditLogResponse> result = useCase.listarRecientes(50);

            assertThat(result).allMatch(r -> r.getNombreUsuario() != null);
        }
    }

    @Nested
    @DisplayName("listarTiposEventos")
    class ListarTiposEventosTests {

        @Test
        @DisplayName("Lista todos los tipos de eventos")
        void lista_todos_los_tipos() {
            List<String> tipos = useCase.listarTiposEventos();

            assertThat(tipos).hasSize(16);
            assertThat(tipos).contains(
                    "LOGIN_SUCCESS",
                    "LOGIN_FAILED",
                    "LOGOUT",
                    "TOKEN_REFRESH",
                    "ACCOUNT_LOCKED",
                    "DASHBOARD_ADMIN_ACCESS",
                    "SESSIONS_INVALIDATED",
                    "SESSION_INVALIDATED",
                    "TIPO_CREDITO_CREADO",
                    "TIPO_CREDITO_ACTUALIZADO",
                    "TIPO_CREDITO_ACTIVADO",
                    "TIPO_CREDITO_DESACTIVADO",
                    "ADMIN_CREADO",
                    "ADMIN_ACTUALIZADO",
                    "ADMIN_ACTIVADO",
                    "ADMIN_DESACTIVADO"
            );
        }

        @Test
        @DisplayName("Tipos de eventos son únicos")
        void tipos_eventos_unicos() {
            List<String> tipos = useCase.listarTiposEventos();

            assertThat(tipos).doesNotHaveDuplicates();
        }
    }
}